/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.event.knowlegebase.impl;

import org.drools.KnowledgeBase;
import org.drools.definition.rule.Rule;
import org.drools.event.knowledgebase.AfterRuleAddedEvent;

public class AfterRuleAddedEventImpl extends KnowledgeBaseEventImpl implements AfterRuleAddedEvent {
    private Rule rule;
    
    public AfterRuleAddedEventImpl(KnowledgeBase knowledgeBase, Rule rule) {
        super( knowledgeBase );
        this.rule = rule;
    }

    public Rule getRule() {
        return this.rule;
    }

	@Override
	public String toString() {
		return "==>[AfterRuleAddedEventImpl: getRule()=" + getRule() + ", getKnowledgeBase()=" + getKnowledgeBase()
				+ "]";
	}
    
}
