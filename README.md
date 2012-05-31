# esi4j-example

## Introduction

esi4j-example is a simple example project integrating [Spring](http://www.springsource.org/), [Hibernate](http://www.hibernate.org/) and [elasticsearch](http://www.elasticsearch.org/) using [esi4j](https://github.com/molindo/esi4j) (elasticsearch integration for Java). It comes as a simple web application with a [Wicket](http://wicket.apache.org/) UI. 

## Functionality

The example app fetches RSS and Atom feeds from URLs and stores the articles into an [H2 database](http://h2database.com/). It's configured to use esi4j's built-in [Hibernate module](https://github.com/molindo/esi4j/tree/master/src/main/java/at/molindo/esi4j/module/hibernate) to (asynchonously) stream data to an elasticsearch index.

## Getting Started

Checkout the [at.molindo.esi4j.example.search package](https://github.com/molindo/esi4j-example/tree/master/src/main/java/at/molindo/esi4j/example/search) to get started with esi4j.