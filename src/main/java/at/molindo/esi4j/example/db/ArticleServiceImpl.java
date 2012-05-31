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
package at.molindo.esi4j.example.db;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jsoup.Jsoup;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.annotation.Transactional;

import at.molindo.esi4j.example.model.Article;

import com.google.common.collect.Lists;
import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.FetcherException;
import com.sun.syndication.fetcher.impl.HashMapFeedInfoCache;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
import com.sun.syndication.io.FeedException;

public class ArticleServiceImpl implements IArticleService, InitializingBean {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ArticleServiceImpl.class);

	private IArticleDAO _articleDAO;

	private HashMapFeedInfoCache _feedInfoCache;
	private HttpURLFeedFetcher _feedFetcher;

	@Override
	public void afterPropertiesSet() throws Exception {
		_feedInfoCache = new HashMapFeedInfoCache();
		_feedFetcher = new HttpURLFeedFetcher(_feedInfoCache);
		_feedFetcher.setUserAgent("esi4j-example fetcher");
	}

	@Override
	@Transactional
	public void index(URL feedUrl) {
		List<Article> articles = toArticles(retrieve(feedUrl));
		for (Article article : articles) {
			_articleDAO.save(article);
		}
		log.info("indexed " + articles.size() + " articles from " + feedUrl);
	}

	private SyndFeed retrieve(URL feedUrl) {
		try {
			SyndFeed feed = _feedFetcher.retrieveFeed(feedUrl);
			log.info("fetched " + feed.getUri());
			return feed;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FeedException e) {
			e.printStackTrace();
		} catch (FetcherException e) {
			e.printStackTrace();
		}
		return null;
	}

	private List<Article> toArticles(SyndFeed feed) {
		ArrayList<Article> articles = Lists.newArrayList();
		for (Object o : feed.getEntries()) {
			SyndEntry entry = (SyndEntry) o;
			String subject = entry.getTitle();
			String url = entry.getUri();
			Date date = entry.getPublishedDate();
			if (date == null) {
				date = entry.getUpdatedDate();
			}
			String body = null;
			if (entry.getContents().size() > 0) {
				body = ((SyndContent) entry.getContents().get(0)).getValue();
			} else if (entry.getDescription() != null) {
				body = entry.getDescription().getValue();
			}

			Article article = new Article(subject, url, date, Jsoup.parse(body).text());

			List<?> categories = entry.getCategories();
			if (categories != null && categories.size() > 0) {
				for (Object c : categories) {
					SyndCategory category = (SyndCategory) c;
					article.getCategories().add(category.getName());
				}
			}

			articles.add(article);
		}
		return articles;
	}

	@Override
	@Transactional
	public void deleteArticles() {
		_articleDAO.deleteArticles();
	}

	public void setArticleDAO(IArticleDAO articleDAO) {
		_articleDAO = articleDAO;
	}

}
