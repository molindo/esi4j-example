/**
 * Copyright 2010 Molindo GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.molindo.esi4j.example.search;

import java.util.List;

import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder.Operator;
import org.elasticsearch.search.facet.FacetBuilders;

import at.molindo.esi4j.action.SearchResponseWrapper;
import at.molindo.esi4j.core.Esi4J;
import at.molindo.esi4j.core.Esi4JOperation;
import at.molindo.esi4j.example.model.Article;
import at.molindo.utils.data.StringUtils;

public class SearchServiceImpl implements ISearchService {

	private Esi4J _esi4j;

	@Override
	public ListenableActionFuture<SearchResponseWrapper> search(final String query, final List<String> categories) {
		return _esi4j.getIndex().executeSearch(new Esi4JOperation<ListenableActionFuture<SearchResponse>>() {

			@Override
			public ListenableActionFuture<SearchResponse> execute(Client client, String indexName,
					OperationContext context) {

				SearchRequestBuilder request = client.prepareSearch(indexName).setTypes(
						context.findTypeMapping(Article.class).getTypeAlias());
				request.setFrom(0).setSize(20);

				QueryBuilder queryBuilder;
				if (!StringUtils.empty(query)) {
					queryBuilder = QueryBuilders.queryString(query).defaultOperator(Operator.AND);
				} else {
					queryBuilder = QueryBuilders.matchAllQuery();
				}

				if (!categories.isEmpty()) {
					FilterBuilder filterBuilder;
					if (categories.size() == 1) {
						filterBuilder = FilterBuilders.termFilter("category", categories.get(0));
					} else {
						BoolFilterBuilder bool = FilterBuilders.boolFilter();
						for (String category : categories) {
							bool.must(FilterBuilders.termFilter("category", category));
						}
						filterBuilder = bool;
					}
					queryBuilder = QueryBuilders.filteredQuery(queryBuilder, filterBuilder);
				}
				request.setQuery(queryBuilder);

				request.addFacet(FacetBuilders.termsFacet("categories").field("category").size(20));

				request.addHighlightedField("body").setHighlighterPreTags("<strong>")
						.setHighlighterPostTags("</strong>");

				return request.execute();
			}
		});
	}

	@Override
	public void rebuild() {
		_esi4j.getIndex().getIndexManager().rebuild();
	}

	@Override
	public void refresh() {
		_esi4j.getIndex().getIndexManager().refresh();
	}

	public void setEsi4j(Esi4J esi4j) {
		_esi4j = esi4j;
	}

}
