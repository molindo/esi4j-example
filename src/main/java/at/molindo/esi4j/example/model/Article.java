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
package at.molindo.esi4j.example.model;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.hibernate.annotations.BatchSize;

import com.google.common.collect.Lists;

@Entity
@BatchSize(size = 20)
public class Article {

	private Long _id;
	private Long _version;
	private String _subject;
	private String _url;
	private Date _date;
	private String _body;
	private List<String> _categories;

	public Article() {
	};

	public Article(String subject, String url, Date date, String body) {
		setSubject(subject);
		setUrl(url);
		setDate(date);
		setBody(body);
	}

	@Id
	@GeneratedValue
	public Long getId() {
		return _id;
	}

	public void setId(Long id) {
		_id = id;
	}

	@Version
	public Long getVersion() {
		return _version;
	}

	public void setVersion(Long version) {
		_version = version;
	}

	public String getSubject() {
		return _subject;
	}

	public void setSubject(String subject) {
		_subject = subject;
	}

	public String getUrl() {
		return _url;
	}

	public void setUrl(String url) {
		_url = url;
	}

	@Temporal(TemporalType.TIME)
	public Date getDate() {
		return _date;
	}

	public void setDate(Date date) {
		_date = date;
	}

	@Column(columnDefinition = "TEXT")
	public String getBody() {
		return _body;
	}

	public void setBody(String body) {
		_body = body;
	}

	@BatchSize(size = 20)
	@ElementCollection
	public List<String> getCategories() {
		if (_categories == null) {
			_categories = Lists.newArrayList();
		}
		return _categories;
	}

	public void setCategories(List<String> categories) {
		_categories = categories;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_date == null) ? 0 : _date.hashCode());
		result = prime * result + ((_subject == null) ? 0 : _subject.hashCode());
		result = prime * result + ((_url == null) ? 0 : _url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Article)) {
			return false;
		}
		Article other = (Article) obj;
		if (_date == null) {
			if (other._date != null) {
				return false;
			}
		} else if (!_date.equals(other._date)) {
			return false;
		}
		if (_subject == null) {
			if (other._subject != null) {
				return false;
			}
		} else if (!_subject.equals(other._subject)) {
			return false;
		}
		if (_url == null) {
			if (other._url != null) {
				return false;
			}
		} else if (!_url.equals(other._url)) {
			return false;
		}
		return true;
	}

}
