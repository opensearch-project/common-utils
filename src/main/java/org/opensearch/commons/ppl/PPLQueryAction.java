/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.commons.ppl;

import org.opensearch.action.ActionType;

/**
 * This is a copied, reduced version of SQL Plugin's PPLQueryAction
 */
public class PPLQueryAction extends ActionType<TransportPPLQueryResponse> {
    public static final String NAME = "cluster:admin/opensearch/ppl";
    public static final PPLQueryAction INSTANCE = new PPLQueryAction();

    private PPLQueryAction() {
        super(NAME, TransportPPLQueryResponse::new);
    }
}
