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

import java.io.File;
import java.util.UUID;

import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.hibernate.SessionFactory;

import at.molindo.esi4j.core.Esi4JIndexManager;
import at.molindo.esi4j.core.impl.DefaultEsi4J;
import at.molindo.esi4j.core.internal.InternalIndex;
import at.molindo.esi4j.module.hibernate.AsyncHibernateProcessingChain;
import at.molindo.esi4j.module.hibernate.HibernateEntityResolver;
import at.molindo.esi4j.module.hibernate.HibernateIndexManager;
import at.molindo.esi4j.spring.Esi4JBean;
import at.molindo.utils.io.FileUtils;
import at.molindo.utils.properties.SystemProperty;

public class ExampleEsi4JBean extends Esi4JBean {

	private SessionFactory _sessionFactory;

	final File _tmpDir;

	public ExampleEsi4JBean() {
		// @noformat

		_tmpDir = new File(SystemProperty.JAVA_IO_TMPDIR.getFile(), "esi4j-example-" + UUID.randomUUID().toString());
		if (!_tmpDir.mkdirs()) {
			throw new RuntimeException("failed to create temp dir: " + _tmpDir);
		}

		// local and ram
		Builder settings = ImmutableSettings.settingsBuilder()
			.put("esi4j.client.type", "node")
			.put("node.data", true)
			.put("node.local", true)
			.put("gateway.type", "none")
			.put("path.data", new File(_tmpDir, "data").toString())
			.put("path.logs", new File(_tmpDir, "logs").toString())
			.put("index.store.type", "ram")
			.put("index.mapper.dynamic", false)
			.put("index.number_of_replicas", 0)
			.put("index.number_of_shards", 1)
			.put("index.refresh_interval", -1);

		// default index html analyzer
		settings
			.put("index.analysis.analyzer.html.type", "standard")
			.put("index.analysis.analyzer.html.char_filter", "html_strip");

		setSettings(settings.build());

		// @format
	}

	@Override
	protected void init(DefaultEsi4J esi4j) {
		if (_sessionFactory == null) {
			throw new IllegalStateException("sessionFactory not configured");
		}

		InternalIndex index = esi4j.getIndex();
		index.addTypeMapping(new ArticleTypeMapping());

		AsyncHibernateProcessingChain processingChain = new AsyncHibernateProcessingChain(index,
				new HibernateEntityResolver(_sessionFactory));

		Esi4JIndexManager hibernateIndexManager = new HibernateIndexManager(_sessionFactory, index, processingChain);
		esi4j.registerIndexManger(hibernateIndexManager);
	}
	
	

	protected void close() {
		FileUtils.delete(_tmpDir);
	}

	public SessionFactory getSessionFactory() {
		return _sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		_sessionFactory = sessionFactory;
	}

}
