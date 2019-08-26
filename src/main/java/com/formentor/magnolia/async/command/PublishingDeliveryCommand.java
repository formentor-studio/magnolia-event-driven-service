package com.formentor.magnolia.async.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formentor.magnolia.async.service.QueueProducerService;
import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.config.registry.DefinitionProvider;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.publishing.command.PublicationCommand;
import info.magnolia.context.Context;
import info.magnolia.rest.EndpointDefinition;
import info.magnolia.rest.delivery.jcr.filter.FilteringContentDecoratorBuilder;
import info.magnolia.rest.delivery.jcr.filter.NodeTypesPredicate;
import info.magnolia.rest.delivery.jcr.v2.JcrDeliveryEndpointDefinition;
import info.magnolia.rest.registry.EndpointDefinitionRegistry;
import info.magnolia.rest.service.node.v1.RepositoryMarshaller;
import info.magnolia.rest.service.node.v1.RepositoryNode;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.jcr.Node;
import java.io.File;

@Slf4j
public class PublishingDeliveryCommand extends PublicationCommand {

    private RepositoryMarshaller marshaller = new RepositoryMarshaller();

    private final QueueProducerService queueProducerService;
    private final EndpointDefinitionRegistry endpointRegistry;

    @Inject
    public PublishingDeliveryCommand(VersionManager versionManager, ComponentProvider componentProvider, QueueProducerService queueProducerService, EndpointDefinitionRegistry endpointRegistry) {
        super(versionManager, componentProvider);
        this.queueProducerService = queueProducerService;
        this.endpointRegistry = endpointRegistry;
    }

    @Override
    public boolean execute(Context context) throws Exception {
        Node node = getJCRNode(context);

        // For all registered endpoints check that the repository and node type is listed
        for (DefinitionProvider<EndpointDefinition> provider : endpointRegistry.getAllProviders()) {
            // Only endpoints registered in REST Content Delivery
            if (provider.get() instanceof JcrDeliveryEndpointDefinition) {
                try {
                    JcrDeliveryEndpointDefinition deliveryDefinition = (JcrDeliveryEndpointDefinition) provider.get();

                    NodeTypesPredicate nodeTypesPredicate = new NodeTypesPredicate(deliveryDefinition.getNodeTypes(), !deliveryDefinition.isStrict());
                    boolean nodeTypeMatched = nodeTypesPredicate.evaluateTyped(node);

                    if (deliveryDefinition.getWorkspace().equals(getRepository()) && nodeTypeMatched) {
                        // Build topic name from the endpoint, build payload from JCR Node and enqueue the event
                        String endpointReferenceId = provider.getMetadata().getReferenceId();
                        String topic = buildTopicName(endpointReferenceId);
                        String payload = buildPayload(node, deliveryDefinition);
                        if (payload != null) {
                            queueProducerService.enqueueMessage(topic, node.getIdentifier(), payload);
                        }
                    }
                } catch (Exception e) {
                    log.error("Failed to publish [{}]", provider.getMetadata().getReferenceId(), e);
                    // Others should continue to be registered.
                }
            }
        }

        return true;
    }

    /**
     * Builds message payload based on a JCR Node
     *
     * @param node
     * @param deliveryDefinition
     * @return
     */
    private String buildPayload(Node node, JcrDeliveryEndpointDefinition deliveryDefinition) {
        FilteringContentDecoratorBuilder decorators = new FilteringContentDecoratorBuilder()
                .childNodeTypes(deliveryDefinition.getChildNodeTypes())
                .strict(deliveryDefinition.isStrict())
                .depth(deliveryDefinition.getDepth())
                .includeSystemProperties(deliveryDefinition.isIncludeSystemProperties());
        String payload = null;
        Node wrappedNode = decorators.wrapNode(node);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            final RepositoryNode response = marshaller.marshallNode(wrappedNode);
            payload = objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            log.error("Failed parsing of [{}] to string", wrappedNode, e);
        }
        return payload;

    }
    /**
     * Build topic name from preference id and definition path.
     * Convention:
     * /module_name/restEndpoints/p1/p2/p3/def_v1.yaml -> p1_p2_p3_def_v1
     *
     * @return topic name
     */
    private String buildTopicName(String endpointReferenceId) {
        return endpointReferenceId.replaceAll(File.separator, "_");
    }

    /**
     * Returns true if nodeType is in list of nodetypes of delivery endpoint configuration.
     *
     * @param nodeType
     * @param jcrDefinition
     * @return
     */
    private boolean isNodeTypeofEndpoint(final String nodeType, JcrDeliveryEndpointDefinition jcrDefinition) {
        return jcrDefinition.getNodeTypes().stream().anyMatch(nt -> nt.equals(nodeType));
    }
}