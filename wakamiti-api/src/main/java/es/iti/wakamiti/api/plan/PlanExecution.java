package es.iti.wakamiti.api.plan;

import lombok.*;

@Getter @Setter
public class PlanExecution {

	private String organization;
	private String project;
	private PlanNode plan;

}
