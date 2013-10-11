# Assembly for XML

**Xembly** is an [Assembly](http://en.wikipedia.org/wiki/Assembly_language)-like
[imperative](http://en.wikipedia.org/wiki/Imperative_programming) programming language
for data manipulation in XML documents. For example, you have an XML document:

```xml
<orders>
  <order id="553">
    <amount>$45.00</amount>
  </order>
</orders>
```

And you want to change the amount of the order #553
from `$45.00` to `$140.00`. Xembly script would look like:

```
XPATH "orders/order[@id=553]";
SET "$140.00";
```

It is much simpler and compact than
[XSLT](http://www.w3.org/TR/xslt) or [XQuery](http://www.w3.org/TR/xquery/).

This Java package implements Xembly:

```java
Document document = DocumentBuilderFactory.newInstance()
  .newDocumentBuilder().newDocument();
dom.appendChild(dom.createElement("orders"));
Iterable<Directive> dirs = new Directives(
  "ADD 'order'; ATTR 'id', '553'; SET '$140.00';"
);
new Xembler(dirs).apply(document);
```

Since version 0.9 you can directly transform directives to XML:

```java
String xml = new Xembler(
  new Directives()
    .xpath("/root")
    .add("order")
    .attr("id", "553")
    .set("$140.00")
).xml("root");
```

## Download and Maven Dependency

Just use this dependency in Maven:

```xml
<dependency>
  <groupId>com.jcabi.incubator</groupId>
  <artifactId>xembly</artifactId>
  <version>0.9</version>
</dependency>
```

Or download the latest release
[here](https://github.com/yegor256/xembly/releases).

## How to contribute

Fork repository, make changes, send us a pull request. We will review
your changes and apply them to the `master` branch shortly, provided
they don't violate our quality standards. To avoid frustration, before
sending us your pull request please run full Maven build:

```
$ mvn clean install -Pqulice
```

## Got questions?

If you have questions or general suggestions, don't hesitate to submit
a new [Github issue](https://github.com/yegor256/xembly/issues/new).
