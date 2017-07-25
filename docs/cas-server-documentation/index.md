---
layout: default
title: CAS - Home
---

# Enterprise Single Sign-On

* Spring Webflow/Spring Boot [Java server component](planning/Architecture.html).
* [Pluggable authentication support](Configuring-Authentication-Components.html) ([LDAP](installation/LDAP-Authentication.html), 
[Database](installation/Database-Authentication.html), [X.509](installation/X509-Authentication.html), [SPNEGO](installation/SPNEGO-Authentication.html), 
[JAAS](installation/JAAS-Authentication.html), [JWT](installation/JWT-Authentication.html), 
[RADIUS](installation/RADIUS-Authentication.html), [MongoDb](installation/MongoDb-Authentication.html), etc)
* Support for multiple protocols ([CAS](protocol/CAS-Protocol.html), [SAML](protocol/SAML-Protocol.html), [WS-Federation](protocol/WS-Federation-Protocol.html),
[OAuth2](protocol/OAuth-Protocol.html), [OpenID](protocol/OpenID-Protocol.html), [OpenID Connect](protocol/OIDC-Protocol.html))
* Support for [multifactor authentication](installation/Configuring-Multifactor-Authentication.html) via a variety of 
providers ([Duo Security](installation/DuoSecurity-Authentication.html), [FIDO U2F](installation/FIDO-U2F-Authentication.html), 
[YubiKey](installation/YubiKey-Authentication.html), [Google Authenticator](installation/GoogleAuthenticator-Authentication.html), etc)
* Support for [delegated authentication](integration/Delegate-Authentication.html) to external providers such as [ADFS](integration/ADFS-Integration.html), Facebook, Twitter, SAML2 IdPs, etc.
* [Monitor and track](installation/Monitoring-Statistics.html) application behavior, statistics and logs in real time.
* Manage and register [client applications and services](installation/Service-Management.html) with specific authentication policies.
* [Cross-platform client support](integration/CAS-Clients.html) (Java, .Net, PHP, Perl, Apache, etc).
* Integrations with [InCommon, Box, Office365, ServiceNow, Salesforce, Workday, WebAdvisor](integration/Configuring-SAML-SP-Integrations.html), Drupal, Blackboard, Moodle, [Google Apps](integration/Google-Apps-Integration.html), etc.

CAS provides a friendly open source community that actively supports and contributes to the project.
While the project is rooted in higher-ed open source, it has grown to an international audience spanning
Fortune 500 companies and small special-purpose installations.

## Getting Started

We recommend reading the following documentation in order to plan and execute a CAS deployment.

* [Architecture](planning/Architecture.html)
* [Getting Started](planning/Getting-Started.html)
* [Installation Requirements](planning/Installation-Requirements.html)
* [Overlay Installation](installation/Maven-Overlay-Installation.html)
* [Authentication](installation/Configuring-Authentication-Components.html)
* [Application Registration](installation/Service-Management.html)
* [Attribute Release](integration/Attribute-Release.html)

## Demos

The following demos are provided by the Apereo CAS project:

| Demo                    | Source Branch            | Location
|-------------------------|--------------------------|----------------------------------------------------
| [CAS Web Application](index.html)     | `heroku-caswebapp`       | [![](https://heroku-badge.herokuapp.com/?app=jasigcas&root=/cas/login)](https://jasigcas.herokuapp.com/cas)
| [CAS Services Management](installation/Installing-ServicesMgmt-Webapp.html) | `heroku-mgmtwebapp`      | [![](https://heroku-badge.herokuapp.com/?app=jasigcasmgmt&root=/cas-management/login)](https://jasigcasmgmt.herokuapp.com/cas-management) 
| [CAS Boot Administration](installation/Configuring-Monitoring-Administration.html) | `heroku-bootadminserver` | [![](https://heroku-badge.herokuapp.com/?app=casbootadminserver&)](https://casbootadminserver.herokuapp.com/)
| [CAS Zipkin](installation/Monitoring-Statistics.html#distributed-tracing) | `heroku-zipkinserver`    | [![](https://heroku-badge.herokuapp.com/?app=caszipkinserver)](https://caszipkinserver.herokuapp.com/)
| [CAS Discovery](installation/Service-Discovery-Guide.html) | `heroku-discoveryserver`    | [![](https://heroku-badge.herokuapp.com/?app=caseureka)](https://caseureka.herokuapp.com/)

It is important to note that these are public demo sites, used by the project for basic showcases and integration tests. They are **NOT** set up for internal demos as they may go up and down as the project needs without notice. 

If you have a need for a demo instance with a modified UI, that would be one you [set up for your deployment](installation/Maven-Overlay-Installation.html). 

## Development

CAS development is powered by: <br/>

<a href="http://www.jetbrains.com/idea/" target="_blank"><img src="../images/intellijidea.gif" valign="middle" style="vertical-align:middle"></a>
