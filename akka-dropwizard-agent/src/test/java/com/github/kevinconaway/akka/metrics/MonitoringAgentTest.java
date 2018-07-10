package com.github.kevinconaway.akka.metrics;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.dispatch.DequeBasedMessageQueue;
import akka.dispatch.Envelope;
import akka.dispatch.MessageQueue;
import akka.dispatch.UnboundedDequeBasedMailbox;
import akka.dispatch.UnboundedMailbox;
import akka.testkit.javadsl.TestKit;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import net.bytebuddy.agent.ByteBuddyAgent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import scala.Option;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class MonitoringAgentTest {

    private Instrumentation instrumentation;
    private ClassFileTransformer transformer;
    private MetricRegistry metricRegistry;
    private ActorSystem actorSystem;
    private TestKit tk;

    @Before
    public void setUp() {
        instrumentation = ByteBuddyAgent.install();
        metricRegistry = new MetricRegistry();

        AkkaDropwizard.configure(
            metricRegistry,
            new AkkaDropwizardSettings(new DefaultActorMetricPrefixStrategy(), "testing-metrics")
        );

        transformer = MonitoringAgent
            .builder()
            .installOn(instrumentation);

        actorSystem = ActorSystem.create("testing");
        tk = new TestKit(actorSystem);
    }

    @After
    public void tearDown() {
        instrumentation.removeTransformer(transformer);

        TestKit.shutdownActorSystem(actorSystem);

        AkkaDropwizard.clear();
    }

    @Test
    public void testInstrumentEnvelope() {
        Envelope envelope = Envelope.apply("", ActorRef.noSender());

        assertThat(envelope).isInstanceOf(MonitorableEnvelope.class);
    }

    @Test
    public void testCreateMessageQueue() {
        UnboundedMailbox mailbox = UnboundedMailbox.apply();
        MessageQueue queue = mailbox.create(Option.apply(ActorRef.noSender()), Option.empty());

        assertThat(queue).isInstanceOf(MonitorableMessageQueue.class);
    }

    @Test
    public void testDequeueReturnsNull() {
        UnboundedMailbox mailbox = UnboundedMailbox.apply();
        MessageQueue queue = mailbox.create(Option.apply(ActorRef.noSender()), Option.empty());

        assertThat(queue.dequeue()).isNull();
    }

    @Test
    public void testDequeueTiming() {
        ActorRef actor = actorSystem.actorOf(Props.create(EchoActor.class), "echo-actor");

        actor.tell("hello world", tk.getRef());

        tk.expectMsgClass(String.class);

        Timer timer = metricRegistry.timer("testing-metrics/user/echo-actor/message-wait");

        assertThat(timer.getCount()).isEqualTo(1);
    }

    @Test
    public void testMailboxSizeCounterRegistered() {
        ActorRef actor = actorSystem.actorOf(Props.create(EchoActor.class), "echo-actor");

        actor.tell("hello world", tk.getRef());

        tk.expectMsgClass(String.class);

        assertThat(metricRegistry.getGauges()).containsKey("testing-metrics/user/echo-actor/mailbox-size");
    }

    @Test
    public void testCleanup() {
        ActorRef actor = actorSystem.actorOf(Props.create(EchoActor.class), "echo-actor");

        tk.watch(actor);

        actor.tell(PoisonPill.getInstance(), tk.getRef());

        tk.expectTerminated(actor);

        assertThat(metricRegistry.getGauges()).doesNotContainKey("testing-metrics/user/echo-actor/mailbox-size");
        assertThat(metricRegistry.getTimers()).doesNotContainKey("testing-metrics/user/echo-actor/message-wait");
    }

    @Test
    public void testParseArguments() {
        Map<String, String> arguments = MonitoringAgent.parseArguments("debug=true,foo=bar,key=value=withequals");

        assertThat(arguments).hasSize(3);
        assertThat(arguments).containsEntry("debug", "true");
        assertThat(arguments).containsEntry("foo", "bar");
        assertThat(arguments).containsEntry("key", "value=withequals");
    }

    @Test
    public void testParseArguments_ValueNoEquals() {
        Map<String, String> arguments = MonitoringAgent.parseArguments("debug=true,foo");

        assertThat(arguments).hasSize(1);
        assertThat(arguments).containsEntry("debug", "true");
    }

    @Test
    public void testParseArguments_Empty() {
        assertThat(MonitoringAgent.parseArguments(null)).isEmpty();
        assertThat(MonitoringAgent.parseArguments("")).isEmpty();
    }

    @Test
    public void testDequeBasedMailbox() {
        ActorRef actor = actorSystem.actorOf(Props.create(EchoActor.class), "echo-actor");

        UnboundedDequeBasedMailbox mailbox = UnboundedDequeBasedMailbox.apply();

        DequeBasedMessageQueue queue = (DequeBasedMessageQueue) mailbox.create(Option.apply(actor), Option.apply(actorSystem));

        Envelope envelope = Envelope.apply("test", actor);

        queue.enqueueFirst(ActorRef.noSender(), envelope);

        Object dequeued = queue.dequeue();

        assertThat(dequeued).isInstanceOf(MonitorableEnvelope.class);

        MonitorableEnvelope monitorableEnvelope = (MonitorableEnvelope) dequeued;

        assertThat(monitorableEnvelope.getEnqueueTime()).isNotNull();

    }

    private static class EchoActor extends AbstractActor {
        @Override
        public Receive createReceive() {
            return receiveBuilder()
                .matchAny(m -> getSender().tell(m, getSelf()))
                .build();
        }
    }
}
