# KMF Storage Plugin: Chronicle backend

This KMF plugin is build around the Chronicle Map project.
This backend is only available for Java SE platforms and directly target in-memory low-latency applications.
Notably, it implements and OffHeap strategy that can be use to manage TBs of in-memory data on a single computer.
This plugin does not rely on an external server.

## Last versions:

- 4.27.0 compatible with KMF framework 4.27.x

## Changelog

- 4.27.0 use Chronicle in version 3.0

## Dependency

Simply add the following dependency to your maven project:

```java
<dependency>
    <groupId>org.kevoree.modeling.plugin</groupId>
    <artifactId>chronicle</artifactId>
    <version>REPLACE_BY_LAST_VERSION</version>
</dependency>
```

## Usage

The ChroniclePlugin is the main entry point for this plugin.
This class take two constructor argument.
The first one (long) is the number of model elements that will have to be managed.
The second one (optional) is the persistent storage if any. In case of null value, pure in-memory strategy will be used.

```java
import org.kevoree.modeling.cdn.KContentDeliveryDriver;
import org.kevoree.modeling.plugin.LevelDBPlugin;

KContentDeliveryDriver levelDBDriver = 
	new ChroniclePlugin(10000000,new File("/tmp/storageFile"));
model = new MyModel(
    DataManagerBuilder.create()
    .withContentDeliveryDriver(levelDBDriver)
    .build()
    );
```

To have more information about KMF Storage Plugin using, please visit the following tutorial step:

https://github.com/kevoree-modeling/tutorial/tree/master/step2_persistence
