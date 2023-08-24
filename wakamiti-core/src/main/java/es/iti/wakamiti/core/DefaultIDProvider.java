package es.iti.wakamiti.core;

import es.iti.wakamiti.api.IDProvider;
import es.iti.wakamiti.api.plan.PlanNodeID;
import java.util.UUID;

public class DefaultIDProvider implements IDProvider {

	@Override
	public PlanNodeID newPlanNodeID() {
		return new PlanNodeID(UUID.randomUUID().toString());
	}

}
