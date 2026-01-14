# README: Bootiful Spring Boot: Building Production-worthy Systems and Services
by Josh Long

Spring Developer Advocate, Java Champion, Microsoft MVP, Kotlin Google Developer Expert (GDE), alumnus

* [Youtube: "Coffee and Software"](https://www.youtube.com/@coffeesoftware)
* [LinkedIn](https://linkedin.com/in/joshlong)
* [code](https://github.com/joshlong-attic/2026-01-14-bootiful-spring-boot)
* [email](josh@joshlong.com)
* [Twitter/X](https://x.com/@starbuxman)

## preface 
Hi, Spring fans! In this installment we look at the amazing Spring Boot 4 release.

## outline  
* data jdbc
* postgresql jdbc dialect and aot repositories
* jspecify
* spring modulith
* endpoint to adopt a dog 
* api versioning for dogs 
* create graphql endpoint to retreive dogs 
* cant be all about dogs, lets build a [www.catfacts.net/facts](https://www.catfacts.net/api/) client 
* now we can return it from a controller 
* what if things go down? we saw that even the most well-run datacenters can sometimes fail. this happened with cloudflare and aws us-east 1 recently. resilience 
* lets focus on production 
* security (OTT, Passkeys) + MFA
* observability
* efficiency
* performance 
* graalvm


## patterns highlighted in the demo 
* outbox pattern w/ spring modulith
* cqrs with axon
* webauthn with spring security 7 


## value classes

```
value class UnsignedInt {}
UnsignedInt x ; // what value is this?
int x ; // 0
boolean x ; // false
UnsignedInt x, y ; 
IO.println( x + y);
```

`List<Integer>` -> `int []` 

`int x, char, boolean, byte, ...`

* value classes (valhalla)
* type classes (haskell, scala, ocaml, ...)
* `null` restriction
* vector api

## virtual threads vs. reactive

they have some shared goals.

#### reactive 
* easy scale (same)
* errors as data
* easy composition (Publisher*)

#### virtual threads 
* easy scale (same)

#### virtual threads vs. threads

scalability when calling: https://httpbin.org/delay/5

```
// yay
Summary:
  Total:	10.1749 secs
  Slowest:	5.1462 secs
  Fastest:	5.0180 secs
  Average:	5.0846 secs
  Requests/sec:	3.9312

// yuck.
Summary:
  Total:	20.1975 secs
  Slowest:	15.1781 secs
  Fastest:	5.0110 secs
  Average:	7.5890 secs
  Requests/sec:	1.9804
  
  Total data:	16200 bytes
  Size/request:	405 bytes

```

#### virtual threads vs. async/await 

python, javascript, c#, kotlin, typescript (bad)

```
async/await  ~= malloc() / free()
```

How this looks in Kotlin

```kotlin
suspend fun getCustomer (id:Int) : Mono <Customer> {
}

val customer = getCustomer(67).await()
```
how it looks in  c#/python/ts/js:

```javascript
async function getCustomer (id ) :Promise<Customer> {
}

const customer = await getCustomer(67)
```

## resources
* [the Spring Initializr](https://start.spring.io)
* [Axon Framework](https://github.com/AxonFramework/AxonFramework)
* my original InfoQ interview [with Allaird Buijze](https://www.infoq.com/articles/cqrs_with_axon_framework/)
* [Embabel](https://github.com/embabel/embabel-agent)

## things to watch 
* [_Growing the Java Language_](https://www.youtube.com/watch?v=Gz7Or9C0TpM)
* [My GraphQL series on Youtube ("Spring Tips")](https://www.youtube.com/watch?v=EE-5xItDfsg)
* [Bootiful GraalVM](https://www.youtube.com/watch?v=YlMinrvdkT4)
