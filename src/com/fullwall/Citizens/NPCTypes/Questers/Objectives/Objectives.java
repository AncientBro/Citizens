package com.fullwall.Citizens.NPCTypes.Questers.Objectives;

import java.util.ArrayList;
import java.util.List;


public class Objectives {
	private final List<Objective> objectives = new ArrayList<Objective>();

	public Objectives(Objective... objectives) {
		for (Objective obj : objectives) {
			this.objectives.add(obj);
		}
	}

	public Objective current() {
		if (this.objectives.size() > 0)
			return this.objectives.get(0);
		return null;
	}

	public void cycle() {
		if (this.objectives.size() > 0)
			this.objectives.remove(0);
	}

	public boolean isCompleted() {
		return this.objectives.size() == 0;
	}

	public void add(Objective objective) {
		this.objectives.add(objective);
	}
}