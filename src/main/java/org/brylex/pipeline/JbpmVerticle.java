package org.brylex.pipeline;

import org.jbpm.process.workitem.bpmn2.ServiceTaskHandler;
import org.kie.api.KieBase;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.rule.EntryPoint;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.utils.KieHelper;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

public class JbpmVerticle extends Verticle {

    @Override
    public void start() {

        final EventBus eventBus = getVertx().eventBus();

        KieBase kieBase = new KieHelper()
                .addResource(ResourceFactory.newClassPathResource("Deploy.bpmn2"))
                .build();

        final KieSession session = kieBase.newKieSession();
        session.getWorkItemManager().registerWorkItemHandler("Service Task", new ServiceTaskHandler());
        container.logger().info("KIE Session initialized [" + session + "].");
        session.addEventListener(new DefaultProcessEventListener() {
            @Override
            public void afterProcessStarted(ProcessStartedEvent event) {

                final JsonObject json = new JsonObject();
                json.putString("id", event.getProcessInstance().getProcessId());
                json.putString("name", event.getProcessInstance().getProcessName());

                eventBus.publish("job.process.event", json);
            }
        });

        eventBus.registerHandler("pipeline.process.new", new Handler<Message>() {
            @Override
            public void handle(Message message) {

                final ProcessInstance processInstance = session.startProcess("" + message.body());
                container.logger().info("Started processInstance :: " + processInstance + ", ID: " + processInstance.getId());

                final JsonObject reply = new JsonObject();
                reply.putNumber("id", processInstance.getId());
                reply.putString("processId", processInstance.getProcessId());
                reply.putString("processName", processInstance.getProcessName());

                message.reply(reply);
            }
        });

        eventBus.registerHandler("pipeline.process.info", new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> message) {

                final ProcessInstance processInstance = session.getProcessInstance(message.body().getLong("id"));

                
                final JsonObject json = new JsonObject();
                json.putNumber("id", processInstance.getId());
                json.putString("processId", processInstance.getProcessId());
                json.putString("processName", processInstance.getProcessName());
                json.putString("state", "" + processInstance.getState());

                for (EntryPoint entryPoint : session.getEntryPoints()) {
                    System.err.println(entryPoint.getEntryPointId());
                }

                json.putString("jalla", "" + session.getEntryPoints());

                message.reply(json);
            }
        });

    }
}
