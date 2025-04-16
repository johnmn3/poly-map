# CLJS `poly-map` Benchmarks

This document outlines the performance of `poly-map`s in ClojureScript. If you're looking for the Clojure benchmarks [look here](./clj-bench.md).

###  Baseline Read: Large Map 

- Standard Map: 188 ms
- Poly Map: 258 ms
- poly-map is 72% the speed of hash-map 

```clojure
|-------------------------|
|-----------------|       | Poly 72%
|-------------------------| Std (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```


###  Baseline Read: Missing Key 

- Standard Map: 115 ms
- Poly Map: 144 ms
- poly-map is 79% the speed of hash-map 

```clojure
|-------------------------|
|-------------------|     | Poly 79%
|-------------------------| Std (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```


###  Baseline Write: Large Map Update 

- Standard Map: 483 ms
- Poly Map: 462 ms
- poly-map is 104% the speed of hash-map 

```clojure
|-------------------------|
|-------------------------| Poly 104%
|-------------------------| Std (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```


###  Baseline Reduce: Large Map Sum Values 

- Standard Map: 292 ms
- Poly Map: 276 ms
- poly-map is 105% the speed of hash-map 

```clojure
|-------------------------|
|-------------------------| Poly 105%
|-------------------------| Std (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```


###  Baseline Reduce: Large Map Sum Values 

- Standard Map: 293 ms
- Poly Map: 287 ms
- poly-map is 102% the speed of hash-map 

```clojure
|-------------------------|
|-------------------------| Poly 102%
|-------------------------| Std (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```


###  Baseline Reduce: Large Map Sum Values 

- Standard Map: 241 ms
- Poly Map: 284 ms
- poly-map is 84% the speed of hash-map 

```clojure
|-------------------------|
|--------------------|    | Poly 84%
|-------------------------| Std (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```


###  Override Impact: Simple Logging Assoc 

- Standard Map: 154 ms
- Poly Map: 127 ms
- poly-map is 121% the speed of hash-map 

```clojure
|-------------------------|
|-----------------------------| Poly 121%
|-------------------------| Std (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```


###  Override Impact: Simple Assoc 

- Standard Map: 138 ms
- Poly Map: 127 ms
- poly-map is 108% the speed of hash-map 

```clojure
|-------------------------|
|--------------------------| Poly 108%
|-------------------------| Std (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```


###  Override Impact: Validating Assoc - Valid Key 

- Standard Map: 332 ms
- Poly Map: 335 ms
- poly-map is 99% the speed of hash-map 

```clojure
|-------------------------|
|------------------------|| Poly 99%
|-------------------------| Std (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```


###  Compare Baseline Assoc Large 

- Standard Map: 321 ms
- Poly Map: 333 ms
- poly-map is 96% the speed of hash-map 

```clojure
|-------------------------|
|-----------------------| | Poly 96%
|-------------------------| Std (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```


###  Transient: Batch Assoc! 

- Standard Map: 603 ms
- Poly Map: 686 ms
- poly-map is 87% the speed of hash-map 

```clojure
|-------------------------|
|---------------------|   | Poly 87%
|-------------------------| Std (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```


###  Transient: persistent! Cost 

- Standard Map: 47 ms
- Poly Map: 250 ms
- poly-map is 18% the speed of hash-map 

```clojure
|-------------------------|
|----|                    | Poly 18%
|-------------------------| Std (100%)
| 0%  | 25%  | 50% | 75%  | 100%
```
