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

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.document.Field;
import org.elasticsearch.common.joda.time.format.ISODateTimeFormat;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.mapper.core.DateFieldMapper;
import org.elasticsearch.index.mapper.core.StringFieldMapper;
import org.elasticsearch.index.mapper.internal.IdFieldMapper;
import org.elasticsearch.index.mapper.object.ObjectMapper.Dynamic;
import org.elasticsearch.index.mapper.object.RootObjectMapper.Builder;

import at.molindo.esi4j.example.model.Article;
import at.molindo.esi4j.mapping.impl.AbstractLongTypeMapping;

public class ArticleTypeMapping extends AbstractLongTypeMapping<Article> {

	public static final String CATEGORY = "category";
	public static final String DATE = "date";
	public static final String BODY = "body";
	public static final String URL = "url";
	public static final String SUBJECT = "subject";

	public ArticleTypeMapping() {
		this("article");
	}

	public ArticleTypeMapping(String typeAlias) {
		super(typeAlias, Article.class);
	}

	@Override
	protected Long id(Article o) {
		return o.getId();
	}

	@Override
	protected void id(Article o, Long i) {
		o.setId(i);
	}

	@Override
	public boolean isVersioned() {
		return true;
	}

	@Override
	protected Long version(Article o) {
		return o.getVersion();
	}

	@Override
	protected void version(Article o, Long version) {
		o.setVersion(version);
	}

	@Override
	protected void buildMapping(Builder mapper) {
		// @noformat
		mapper
			.dynamic(Dynamic.STRICT)
			.add(new IdFieldMapper.Builder())
			.add(new StringFieldMapper.Builder(SUBJECT))
			.add(new StringFieldMapper.Builder(URL))
			.add(new DateFieldMapper.Builder(DATE))
			.add(new StringFieldMapper.Builder(BODY))
			.add(new StringFieldMapper.Builder(CATEGORY).index(Field.Index.NOT_ANALYZED));
		// @format
	}

	@Override
	protected void writeObject(XContentBuilder builder, Article article) throws IOException {
		// @noformat
		builder
			.field(SUBJECT).value(article.getSubject())
			.field(URL).value(article.getUrl())
			.field(DATE).value(ISODateTimeFormat.dateTime().print(article.getDate().getTime()))
			.field(BODY).value(article.getBody())
			.field(CATEGORY).value(article.getCategories());
		// @format
	}

	@Override
	protected Article readObject(Map<String, Object> source) {
		Article article = new Article();

		article.setSubject((String) source.get(SUBJECT));
		article.setUrl((String) source.get(URL));
		article.setBody((String) source.get(BODY));

		String date = (String) source.get(DATE);
		if (date != null) {
			article.setDate(ISODateTimeFormat.dateTime().parseDateTime(date).toDate());
		}

		Object categories = source.get(CATEGORY);
		if (categories instanceof String) {
			article.getCategories().add((String) categories);
		} else if (categories instanceof Iterable<?>) {
			for (Object o : ((Iterable<?>) categories)) {
				article.getCategories().add((String) o);
			}
		}

		return article;
	}

}
