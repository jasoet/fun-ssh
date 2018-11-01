# Simple Jsch (Java SSH2 implementation) wrapper on Kotlin

[![Build Status](https://travis-ci.org/jasoet/fun-ssh.svg?branch=master)](https://travis-ci.org/jasoet/fun-ssh)
[![codecov](https://codecov.io/gh/jasoet/fun-ssh/branch/master/graph/badge.svg)](https://codecov.io/gh/jasoet/fun-ssh)
[![Download](https://api.bintray.com/packages/jasoet/fun/fun-ssh/images/download.svg)](https://bintray.com/jasoet/fun/fun-ssh/_latestVersion)


## Features
- Kotlin DSL for [Jsch](http://www.jcraft.com/jsch/) (Pure Java SSH2 Implementation).
- Jsch [Example](http://www.jcraft.com/jsch/examples/).
- Execute Remote Command.
- Generate DSA and RSA KeyPair.

## Gradle

### Add JCenter repository
```groovy
repositories {
    jcenter()
}
```

### Add dependency 
```groovy
compile 'id.jasoet:fun-ssh:<version>'
```

## Usage
### Execute simple remote command

### Generate DSA and RSA KeyPair
