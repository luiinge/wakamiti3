package es.iti.wakamiti.core;

import es.iti.wakamiti.api.repository.PlanRepository;
import java.io.*;
import java.util.*;
import java.util.stream.Stream;

import es.iti.wakamiti.api.*;
import es.iti.wakamiti.api.contributors.*;
import es.iti.wakamiti.api.plan.*;
import es.iti.wakamiti.api.resources.BuildPlanRequest;
import imconfig.Config;
import jexten.ExtensionManager;

public class WakamitiEngine implements Wakamiti {


	private static final Log log = Log.of();

	public static WakamitiEngine of(Config config, ExtensionManager extensionManager) {
		return new WakamitiEngine(config, extensionManager);
	}

	public static WakamitiEngine of(Config config) {
		return new WakamitiEngine(config, ExtensionManager.create());
	}

	private final Config config;
	private final ExtensionManager extensionManager;
	private final IDProvider idProvider = new DefaultIDProvider();


	WakamitiEngine(Config config, ExtensionManager extensionManager) {
		this.extensionManager = extensionManager.withInjectionProvider(this::inject);
		this.config = prepareConfiguration(config,this.extensionManager);
	}


	private Config prepareConfiguration(Config config, ExtensionManager extensionManager) {
		Config effectiveConfig = Config.factory().empty();
		for (var provider : extensionManager.getExtensions(ConfigProvider.class).toList()) {
			effectiveConfig = effectiveConfig.accordingDefinitions(provider.config().getDefinitions().values());
		}
		Config envConfig = Config.factory().fromEnvironment().inner("wakamiti");
		effectiveConfig = effectiveConfig.append(envConfig);
		effectiveConfig = effectiveConfig.append(config);

		return effectiveConfig.validate();
	}


	private Stream<Object> inject (Class<?> type, String name) {
		if (type == WakamitiEngine.class)
			return Stream.of(this);
		if (type == Config.class)
			return Stream.of(config);
		if (type == ExtensionManager.class)
			return Stream.of(extensionManager);
		if (type == IDProvider.class)
			return Stream.of(idProvider);
		return Stream.empty();
	}


	private PlanRepository repository() {
		return extensionManager.getExtension(PlanRepository.class).orElseThrow(
			()->new WakamitiException("Cannot instantiate a plan repository")
		);
	}


	public PlanNodeID buildPlan() {
		var resources = extensionManager.getExtensions(ResourceProvider.class)
			.flatMap(it -> it.resources().stream())
			.toList();
		if (resources.isEmpty()) {
			throw new WakamitiException("No resources were found to assemble the test plan");
		}
		log.info("Building test plan using the following resources:");
		resources.forEach(resource ->
			log.info("- {resource} [{contentType}]",resource.relativePath(), resource.contentType()
		));
		var buildPlanRequest = new BuildPlanRequest(config,resources);
		var cachedPlan = repository().getByPlanRequest(buildPlanRequest);
		cachedPlan.ifPresent(
			it -> log.info("Resources have not changed, using previously assembled plan")
		);
		return cachedPlan.orElseGet(()-> assembleNewPlan(buildPlanRequest));
	}



	public void serialize(PlanNodeID planNodeID, Writer writer) throws IOException {
		extensionManager.getExtension(PlanSerializer.class).orElseThrow(
			()-> new WakamitiException("There is no plan serializer available")
		).serializeTree(planNodeID, writer);
	}



	private PlanNodeID assembleNewPlan(BuildPlanRequest buildPlanRequest) {
		var planProviders = extensionManager.getExtensions(PlanProvider.class).toList();
		List<PlanNodeID> providedPlans = new ArrayList<>();
		for (var planProvider : planProviders) {
			var resources = buildPlanRequest.resources().stream().filter(planProvider::accept).toList();
			if (resources.isEmpty()) {
				continue;
			}
			planProvider.providePlan(resources).ifPresent(providedPlans::add);
		}
		if (providedPlans.isEmpty()) {
			throw new WakamitiException("Could not assemble a test plan from the selected resources");
		}
		if (providedPlans.size() == 1) {
			return providedPlans.get(0);
		}
		var root = new PlanNode(NodeType.AGGREGATOR).name("Test Plan");
		var rootID = repository().persist(root);
		providedPlans.forEach(it -> repository().attachChild(rootID,it));
		repository().updateResourceSet(buildPlanRequest, rootID);
		return rootID;
	}



	public ExtensionManager extensionManager() {
		return this.extensionManager;
	}


	public Stream<DataType> dataTypes() {
		return this.extensionManager.getExtensions(DataTypeProvider.class)
			.flatMap(DataTypeProvider::dataTypes);
	}


	public Stream<ContentType> contentTypes() {
		return this.extensionManager.getExtensions(ContentTypeProvider.class)
			.flatMap(ContentTypeProvider::contentTypes);
	}



}
