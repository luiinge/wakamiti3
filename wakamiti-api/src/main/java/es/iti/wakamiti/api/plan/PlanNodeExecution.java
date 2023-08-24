package es.iti.wakamiti.api.plan;

import java.time.Instant;
import lombok.*;

@Getter @Setter @EqualsAndHashCode
public class PlanNodeExecution {

	private NodeResult result;
	private Instant startInstant;
	private Instant finishInstant;

}
