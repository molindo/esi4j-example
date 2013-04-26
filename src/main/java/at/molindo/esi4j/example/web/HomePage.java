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
package at.molindo.esi4j.example.web;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.UrlValidator;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.search.facet.Facets;
import org.elasticsearch.search.facet.terms.TermsFacet;
import org.elasticsearch.search.facet.terms.TermsFacet.Entry;
import org.elasticsearch.search.highlight.HighlightField;

import at.molindo.esi4j.action.SearchHitWrapper;
import at.molindo.esi4j.action.SearchResponseWrapper;
import at.molindo.esi4j.example.db.IArticleService;
import at.molindo.esi4j.example.model.Article;
import at.molindo.esi4j.example.search.ISearchService;
import at.molindo.utils.data.StringUtils;

import com.google.common.collect.Lists;

@SuppressWarnings("serial")
public class HomePage extends WebPage {

	private final IModel<Search> _searchModel;
	private final LoadableDetachableModel<ListenableActionFuture<SearchResponseWrapper>> _searchResponseModel;

	private WebMarkupContainer _container;
	private CheckGroup<String> _facetsContainer;

	@SpringBean
	private IArticleService _articleService;

	@SpringBean
	private ISearchService _searchService;

	public HomePage() {
		add(new UrlSubmissionForm("urlForm"));

		_searchModel = new AbstractReadOnlyModel<Search>() {
			private final Search _search = new Search();

			@Override
			public Search getObject() {
				return _search;
			}
		};

		_searchResponseModel = new LoadableDetachableModel<ListenableActionFuture<SearchResponseWrapper>>() {

			@Override
			protected ListenableActionFuture<SearchResponseWrapper> load() {
				Search search = _searchModel.getObject();
				return _searchService.search(search.getQuery(), search.getCategories());
			}

		};

		IModel<List<SearchHitWrapper>> articlesModel = new AbstractReadOnlyModel<List<SearchHitWrapper>>() {

			@Override
			public List<SearchHitWrapper> getObject() {
				return _searchResponseModel.getObject().actionGet().getHits();
			}

		};

		IModel<List<? extends TermsFacet.Entry>> facetsModel = new AbstractReadOnlyModel<List<? extends TermsFacet.Entry>>() {

			@Override
			public List<? extends TermsFacet.Entry> getObject() {
				Facets facets = _searchResponseModel.getObject().actionGet().getSearchResponse().getFacets();
				if (facets == null) {
					return Collections.emptyList();
				}

				TermsFacet facet = (TermsFacet) facets.facet("categories");
				if (facet == null) {
					return Collections.emptyList();
				}

				return facet.entries();
			}

		};

		add(new TextField<String>("search", new PropertyModel<String>(_searchModel, "query"))
				.add(new OnChangeUpdateSearchBehavior()));

		// category select
		add(_facetsContainer = new CheckGroup<String>("facetsContainer"));
		_facetsContainer.setOutputMarkupId(true).setRenderBodyOnly(false);
		_facetsContainer.add(new ListView<TermsFacet.Entry>("categoryFacets", facetsModel) {

			@Override
			protected IModel<TermsFacet.Entry> getListItemModel(IModel<? extends List<TermsFacet.Entry>> listViewModel,
					int index) {
				return new CompoundPropertyModel<TermsFacet.Entry>(super.getListItemModel(listViewModel, index));
			}

			@Override
			protected void populateItem(final ListItem<Entry> item) {
				CheckBox box;
				item.add(box = new CheckBox("check", new IModel<Boolean>() {

					@Override
					public Boolean getObject() {
						return _searchModel.getObject().getCategories().contains(item.getModelObject().getTerm());
					}

					@Override
					public void setObject(Boolean checked) {
						List<String> categories = _searchModel.getObject().getCategories();
						String category = item.getModelObject().getTerm();
						if (Boolean.TRUE.equals(checked)) {
							categories.add(category);
						} else {
							categories.remove(category);
						}
					}

					@Override
					public void detach() {
					}

				}));
				box.add(new OnChangeUpdateSearchBehavior());

				item.add(new SimpleFormComponentLabel("term", box.setLabel(new PropertyModel<String>(item.getModel(),
						"term"))));
				item.add(new Label("count"));
			}

		});

		// search results
		add(_container = new WebMarkupContainer("container"));
		_container.setOutputMarkupId(true);
		_container.add(new Label("query", _searchModel.getObject().getQuery()));
		_container.add(new ListView<SearchHitWrapper>("result", articlesModel) {

			@Override
			protected IModel<SearchHitWrapper> getListItemModel(IModel<? extends List<SearchHitWrapper>> listViewModel,
					int index) {
				return new CompoundPropertyModel<SearchHitWrapper>(super.getListItemModel(listViewModel, index));
			}

			@Override
			protected void populateItem(final ListItem<SearchHitWrapper> item) {
				item.add(new Label("object.subject"));
				item.add(new Label("object.date"));
				item.add(new Label("object.body", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						SearchHitWrapper wrapper = item.getModelObject();

						HighlightField field = wrapper.getHit().getHighlightFields().get("body");
						if (field == null) {
							return wrapper.getObject(Article.class).getBody();
						}

						Object[] fragments = field.getFragments();
						if (fragments == null) {
							return wrapper.getObject(Article.class).getBody();
						}

						return StringUtils.join(" ... ", fragments);
					}
				}));
				item.add(new ExternalLink("link", new PropertyModel<String>(item.getModel(), "object.url")));
				item.add(new ListView<String>("categories", new PropertyModel<List<String>>(item.getModel(),
						"object.categories")) {

					@Override
					protected void populateItem(ListItem<String> item) {
						item.add(new Label("name", item.getModel()));
					}
				});
			}

		});

		add(new IndicatingAjaxLink<Void>("rebuild") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				_searchService.rebuild();
				updateSearch(target);
			}

		});

		add(new IndicatingAjaxLink<Void>("delete") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				_articleService.deleteArticles();
				_searchService.refresh();
				updateSearch(target);
			}

		});
	}

	private void updateSearch(AjaxRequestTarget target) {
		_searchResponseModel.detach();
		if (target != null) {
			target.add(_container, _facetsContainer);
		}
	}

	@Override
	protected void onDetach() {
		super.onDetach();
		_searchModel.detach();
		_searchResponseModel.detach();
	}

	private final class OnChangeUpdateSearchBehavior extends OnChangeAjaxBehavior {
		@Override
		protected void onUpdate(AjaxRequestTarget target) {
			updateSearch(target);
		}
	}

	private class UrlSubmissionForm extends Form<Void> {

		private TextField<String> _field;

		public UrlSubmissionForm(String id) {
			super(id);

			final FeedbackPanel feedback = new FeedbackPanel("feedback");
			feedback.setOutputMarkupId(true);
			add(feedback);

			add((_field = new TextField<String>("url", new Model<String>("http://www.nytimes.com/services/xml/rss/nyt/HomePage.xml")))
					.add(new UrlValidator()));

			add(new IndicatingAjaxButton("submit") {

				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					try {
						_articleService.index(new URL(_field.getModelObject()));
						_searchService.refresh();
						info("Done");
						target.add(feedback);
						updateSearch(AjaxRequestTarget.get());
					} catch (MalformedURLException e) {
						throw new WicketRuntimeException("invalid URL bypassed validator?", e);
					}
				}

				@Override
				protected void onError(AjaxRequestTarget target, Form<?> form) {
					target.add(feedback);
					updateSearch(AjaxRequestTarget.get());
				}
			});
		}
	}

	public static final class Search implements Serializable {
		private String _query;
		private final List<String> _categories = Lists.newArrayListWithCapacity(3);

		public String getQuery() {
			return _query;
		}

		public void setQuery(String query) {
			this._query = query;
		}

		public List<String> getCategories() {
			return _categories;
		}

	}

}
