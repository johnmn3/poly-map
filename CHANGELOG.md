# Changelog

*The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)*

## [0.1.8 ] - 2025-04-20

### Added

- New user api namespace.
- Added the ability to "freeze" wraps.
- Persistent constructor now available in environment as `<-`
- _"multi-deftype optimization"_ for hot code paths.
- Constructor optimization.

### Fixed

- Fixed some performance regressions.

### Changed

- Renamed library to _`wrap` map_.

### Removed

Old files and naming conventions.

## [0.1.0] - 2025-04-13

### Added

- Initial commit. 
- Migrated logic from ti-yong.

## 0.1.1 - 2025-04-15

### Added

- Introduced High-Level API: Added wrap/assoc and wrap/dissoc functions using simple keywords (e.g., :get, :assoc) for easier customization of common behaviors.
- Added examples-high-level.md and updated documentation.

### Changed

#### Major Performance Optimizations:

- Implemented specialized internal types (WrapMap+...) to significantly speed up baseline assoc and get operations by reducing runtime dispatch overhead.
- Optimized wrap map constructor, especially when called via apply, bringing performance close to native hash-map.
- Improved transient batch assoc! performance to be nearly on par with native transients.
- Improved persistent! performance, though it remains an area with overhead compared to native maps.
- Clojure Benchmark changes:
    - Read Existing Key
        - +2.0% improvement
    - Read Missing Key
        - +59.6% improvement
    - Write (Update Existing Key)
        - +5.5% improvement
    - Reduce (Sum Val)
        - +6.2%
    - Construct (into)
        - +72.0% improvement
    - Construct (apply)
        - +683.6% improvement
    - Simple assoc (Baseline Wrap)
        - +12.9% improvement
    - Simple assoc (Logging Wrap)
        - +115.9% improvement
    - assoc New Key (Baseline Wrap)
        - +1518.7% improvement
    - assoc New Key (Validated Wrap)
        - +1465.1% improvement
    - Batch assoc! (Baseline Wrap)
        - +180.5% improvement
    - Batch assoc! (Logging Wrap)
        - +77.4% improvement
    - persistent! Cost
        - +56.1% improvement
    - Contended Update
        - +135.4% improvement
- ClojureScript Benchmark changes:
    - Read Existing Key (Large Map)
        - +25.2% improvement
    - Read Missing Key
        - +54.9% improvement
    - Write (Update Existing Key - Large Map)
        - +97.3% improvement
    - Reduce (Sum Values - Large Map)
        - -2.6% regression (still faster than vanilla `hash-map`s here though)
    - Simple assoc (Baseline Wrap - Small)
        - +272.4% improvement
    - Simple assoc (Logging Wrap - Small)
        - +318.7% improvement
    - assoc New Key (Baseline Wrap - Large)
        - +82.9% improvement
    - assoc New Key (Validated Wrap - Large)
        - +85.7% improvement
    - Batch assoc! (Baseline Wrap)
        - +12.0% improvement
    - persistent! Cost
        - +36.4% improvement
- Using repo/git workflow from https://bogoyavlensky.com/blog/build-and-publish-clojure-lib-with-slim/
    - Will migrate commands to `bb` tasks in a future release.
