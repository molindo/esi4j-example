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
package at.molindo.esi4j.example;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import at.molindo.esi4j.example.db.ArticleDAOImpl;
import at.molindo.esi4j.example.db.ArticleServiceImpl;
import at.molindo.esi4j.example.db.IArticleDAO;
import at.molindo.esi4j.example.db.IArticleService;
import at.molindo.esi4j.example.search.ExampleEsi4JBean;
import at.molindo.esi4j.example.search.ISearchService;
import at.molindo.esi4j.example.search.SearchServiceImpl;

@Configuration
@ImportResource("classpath:hibernate-daos.xml")
public class Config {

	@Bean(autowire = Autowire.BY_TYPE)
	public IArticleService articleService() {
		return new ArticleServiceImpl();
	}

	@Bean(autowire = Autowire.BY_TYPE)
	public IArticleDAO articleDAO() {
		return new ArticleDAOImpl();
	}

	@Bean(autowire = Autowire.BY_TYPE)
	public ISearchService searchService() {
		return new SearchServiceImpl();
	}

	@Bean(autowire = Autowire.BY_TYPE)
	public ExampleEsi4JBean esi4j() {
		return new ExampleEsi4JBean();
	}

}
