/* ClosedLoop.java
 *
 * Copyright 2009-2019 Comcast Interactive Media, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fishwife.jrugged.control;

class ClosedLoop<E extends Event, M extends Model<E>, A extends Action> {

	private Controller<M, A> controller;
	private M model;

	ClosedLoop(Controller<M, A> controller, M model) {
		this.controller = controller;
		this.model = model;
	}

	void addObjective(Objective<M> objective) {
		controller.addObjective(objective);
	}

	void withdrawObjective(Objective<M> objective) {
		controller.withdrawObjective(objective);
	}

	void processEvent(E event) {
		model.update(event);
		controller.assessObjectives(model);
		A a = controller.selectAction(model);
		a.execute();
	}

}
