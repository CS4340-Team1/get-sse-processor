/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.it.processor.getsse;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.sse.InboundSseEvent;
import jakarta.ws.rs.sse.SseEventSource;
import org.apache.nifi.annotation.behavior.*;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

@TriggerWhenEmpty
@Tags({"SSE, Serve Sent Event"})
@CapabilityDescription("Provides an GET Endpoint for Server Sent Events.")
@ReadsAttributes({@ReadsAttribute(attribute="none", description="Does not Read Attributes")})
@WritesAttributes(
        {
                @WritesAttribute(attribute="id", description="SSE event ID"),
                @WritesAttribute(attribute = "data", description = "Data from SSE event"),
                @WritesAttribute(attribute = "event", description = "Name of SSE event"),
                @WritesAttribute(attribute = "comment", description = "Comment (if any) from SSE event")
        })
public class GetSSE extends AbstractProcessor {
    //URL source of SSE
    public static final PropertyDescriptor URL = new PropertyDescriptor
            .Builder().name("URL")
            .displayName("URL")
            .description("URL of the SSE API emitter")
            .required(true)
            .addValidator(StandardValidators.URL_VALIDATOR)
            .build();

    public static final PropertyDescriptor API_KEY = new PropertyDescriptor
            .Builder().name("API_KEY")
            .displayName("TODO: API Key")
            .description("TODO: API Key of the SSE API emitter")
            .required(false)
            .build();

    public static final PropertyDescriptor BATCH_SIZE = new PropertyDescriptor.Builder()
            .name("Batch Size")
            .description("The maximum number of SSE emitter responses to pull in each iteration")
            .required(true)
            .addValidator(StandardValidators.POSITIVE_INTEGER_VALIDATOR)
            .defaultValue("10")
            .build();

    public static final Relationship RESPONSE = new Relationship.Builder()
            .name("Response")
            .description("Response FlowFiles transferred when receiving HTTP responses with a status code between 200 and 299.")
            .build();

    public static final Relationship FAILURE = new Relationship.Builder()
            .name("Failure")
            .description("Request FlowFiles transferred when receiving socket communication errors.")
            .build();

    private List<PropertyDescriptor> properties;
    private Set<Relationship> relationships;
    private Queue<InboundSseEvent> queue = new LinkedBlockingDeque<>();

    @Override
    protected void init(final ProcessorInitializationContext context){
        properties = new ArrayList<>();
        properties.add(URL);
        properties.add(BATCH_SIZE);
        this.properties = Collections.unmodifiableList(this.properties);

        relationships = new HashSet<>();
        relationships.add(RESPONSE);
        relationships.add(FAILURE);
        this.relationships = Collections.unmodifiableSet(relationships);
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return properties;
    }

    @Override
    public Set<Relationship> getRelationships() {
        return relationships;
    }

    private WebTarget target;
    @OnScheduled
    public void onScheduled(final ProcessContext context) {
        Client client = ClientBuilder.newClient();
        target = client.target(context.getProperty(URL).getValue());
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        final int batchSize = context.getProperty(BATCH_SIZE).asInteger();
        final ComponentLog log = getLogger();

        try{
            try (SseEventSource source = SseEventSource.target(target).build()) {
                source.open();
                source.register(inboundSseEvent -> handleEventQueue(session, inboundSseEvent));
            }
        }catch (final Exception e){
            FlowFile flowFile = session.create();
            log.error(e.getMessage());
            session.putAttribute(flowFile, "error", e.getMessage());
            session.transfer(flowFile, FAILURE);
        }
    }
/*
    public void handleEventQueue(final ProcessSession session, InboundSseEvent inboundSseEvent){
        FlowFile flowFile = session.create();
        Map<String, String> content = new HashMap<>();
        content.put("id", inboundSseEvent.getId());
        content.put("event", inboundSseEvent.getName());
        content.put("data", inboundSseEvent.readData());
        content.put("comment", inboundSseEvent.getComment());

        flowFile = session.putAllAttributes(flowFile, content);
        session.getProvenanceReporter().modifyContent(flowFile);
        session.getProvenanceReporter().route(flowFile, RESPONSE);
        session.transfer(flowFile, RESPONSE);
    }
}
*/
public void handleEventQueue(final ProcessSession session, InboundSseEvent inboundSseEvent){
    FlowFile flowFile = session.create();

    // Extract attributes from the SSE event
    Map<String, String> attributes = new HashMap<>();
    attributes.put("id", inboundSseEvent.getId());
    attributes.put("event", inboundSseEvent.getName());
    attributes.put("comment", inboundSseEvent.getComment());
    flowFile = session.putAllAttributes(flowFile, attributes);

    // Read data from the SSE event and set it as the content of the flow file
    String eventData = inboundSseEvent.readData();
    if (eventData != null && !eventData.isEmpty()) {
        flowFile = session.write(flowFile, out -> out.write(eventData.getBytes()));
    }

    // Transfer the flow file to the RESPONSE relationship
    session.getProvenanceReporter().modifyContent(flowFile);
    session.getProvenanceReporter().route(flowFile, RESPONSE);
    session.transfer(flowFile, RESPONSE);
}
