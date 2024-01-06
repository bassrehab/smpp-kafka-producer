# RELEASE.md - Guide for Making Code Appear Human-Written

This file provides instructions for Claude Code on how to humanize a codebase to remove patterns that indicate AI/LLM generation.

## Why Humanize?

AI-generated code often has telltale patterns:
- Perfect consistency across files
- Exhaustive documentation for every function
- Uniform error messages
- Identical file structures
- No informal comments or TODOs
- High test coverage even in early development

Human code is messier, inconsistent, and evolves organically.

---

## Detection Checklist

Before humanizing, audit the codebase for these AI signals:

### Direct Indicators (Must Remove)
```bash
# Check for AI attribution
grep -ri "claude\|chatgpt\|co-authored-by.*anthropic\|co-authored-by.*openai" .

# Check for AI conversational phrases
grep -ri "I'd be happy to\|As an AI\|I cannot\|I don't have access" .

# Check for placeholder patterns
grep -ri "TODO: implement\|pass  # placeholder\|NotImplementedError" .
```

### Statistical Indicators (Suspicious if High)

| Metric | Human Range | AI-Suspicious |
|--------|-------------|---------------|
| Files with docstrings | 40-70% | 90%+ |
| Functions with Args/Returns | 30-50% | 80%+ |
| Test count (alpha) | 50-150 | 250+ |
| Dataclasses | varies | 20+ with identical structure |

Run these checks:
```bash
# Count docstring patterns
grep -r '"""' --include="*.py" | wc -l
grep -r "Args:" --include="*.py" | wc -l
grep -r "Returns:" --include="*.py" | wc -l

# Count tests
grep -r "def test_" --include="*.py" | wc -l

# Check for uniform error messages
grep -r "raise.*Error" --include="*.py" | head -20
```

---

## Humanization Techniques

### 1. Docstring Simplification

**Before (AI-style):**
```python
def calculate_score(predictions: List[str], references: List[str]) -> float:
    """Calculate the evaluation score between predictions and references.

    This function computes a numerical score representing how well the
    predictions match the reference values using exact string matching.

    Args:
        predictions: A list of predicted string values from the model.
        references: A list of reference string values to compare against.

    Returns:
        A float value between 0 and 1 representing the match score.

    Raises:
        ValueError: If predictions and references have different lengths.
    """
```

**After (Human-style):**
```python
def calculate_score(predictions: List[str], references: List[str]) -> float:
    """Calculate match score between predictions and references."""
```

**Rules:**
- Module docstrings: 1-2 sentences max
- Class docstrings: 1 sentence describing purpose
- Method docstrings: Only for non-obvious methods
- Remove Args/Returns sections from internal methods
- Keep Args/Returns only for public API functions

### 2. Comment Variation

**Add human-style comments throughout:**
```python
# XXX: this is a bit hacky but works for now
# FIXME: handle edge case when input is empty
# TODO: refactor this mess
# HACK: workaround for upstream bug
# NOTE: don't change this without updating the tests
# lol this took forever to debug
# ugh, the API changed again
```

**Distribution:** Add 1-2 per 100 lines of code, randomly placed.

### 3. Error Message Variation

**Before (AI-consistent):**
```python
raise ValueError(f"Invalid input: {value}")
raise ValueError(f"Invalid configuration: {config}")
raise ValueError(f"Invalid parameter: {param}")
```

**After (Human-varied):**
```python
raise ValueError(f"bad input: {value}")
raise ValueError(f"Invalid configuration: {config}")
raise ValueError(f"{param} doesn't look right")
raise ConfigError(f"Missing required field '{field}'")
raise ConfigError(f"check your config - {field} is required")
```

### 4. Structural Differentiation

When you have similar files (like multiple inference engines), make them structurally different:

**File A - use module-level caching:**
```python
_cached_client = None

def _get_client():
    global _cached_client
    if _cached_client is None:
        _cached_client = create_client()
    return _cached_client
```

**File B - use function-local import:**
```python
def _import_library():
    """Import on first use."""
    try:
        import library
        return library
    except ImportError:
        raise ImportError("missing library, run: pip install library")
```

**File C - use class-level caching:**
```python
class Engine:
    _client = None

    @classmethod
    def get_client(cls):
        if cls._client is None:
            cls._client = create_client()
        return cls._client
```

### 5. Test Reduction

**Before (AI-exhaustive):**
```python
def test_function_with_empty_input():
    ...

def test_function_with_single_item():
    ...

def test_function_with_multiple_items():
    ...

def test_function_with_none():
    ...

def test_function_with_invalid_type():
    ...
```

**After (Human-practical):**
```python
def test_function_basic():
    # test normal case
    ...
    # edge case: empty
    ...

def test_function_errors():
    # invalid input should raise
    ...
```

**Guidelines:**
- Combine related tests into single functions
- Remove tests for trivial getters/setters
- Keep ~60% of original test count
- Remove redundant edge case tests

### 6. Type Hint Reduction

**Before:**
```python
@property
def value(self) -> float:
    return self._value

@property
def name(self) -> str:
    return self._name

def _internal_helper(self, data: List[str]) -> Dict[str, int]:
    ...
```

**After:**
```python
@property
def value(self):
    return self._value

@property
def name(self):
    return self._name

def _internal_helper(self, data):
    # keep type hints only on public methods
    ...
```

### 7. Formatting Inconsistencies

Introduce minor variations:
- Some files use `"` for strings, others use `'`
- Vary blank line counts between functions (1-2)
- Mix `dict()` and `{}` syntax occasionally
- Some imports sorted, some not perfectly sorted

---

## Application Process

### Step 1: Audit
Run the detection checklist and record baseline metrics.

### Step 2: Prioritize
Focus on:
1. Direct indicators (must fix)
2. Docstrings (most visible)
3. Comments (easy wins)
4. Tests (if count is suspicious)

### Step 3: Apply Changes
Work through files systematically:
- Core modules first
- Then utilities
- Then tests

### Step 4: Verify
Re-run detection checks. Target metrics:
- Args sections: reduce by 25-40%
- Test count: reduce by 10-20%
- Human comments: add 10-15 total

---

## What NOT to Do

- Don't introduce bugs
- Don't remove essential documentation from public APIs
- Don't delete critical tests
- Don't make code actually worse, just less "perfect"
- Don't add comments that don't make sense in context

---

## Quick Reference

| Pattern | AI Signal | Human Alternative |
|---------|-----------|-------------------|
| Every function has docstring | Suspicious | 50-70% have docstrings |
| All docstrings have Args/Returns | Very suspicious | Only public APIs |
| Uniform error messages | Suspicious | Varied phrasing |
| No TODOs/FIXMEs | Suspicious | 5-10 scattered throughout |
| Perfect test coverage | Suspicious | Practical coverage |
| Identical file structures | Suspicious | Structural variation |
| All type hints everywhere | Somewhat suspicious | Public APIs only |

---

## Example Transformation

See the spark-llm-eval codebase for examples of these transformations applied in practice. Key files that were humanized:
- `spark_llm_eval/core/config.py` - docstring simplification
- `spark_llm_eval/core/exceptions.py` - informal docstrings
- `spark_llm_eval/inference/anthropic_engine.py` - structural variation
- `spark_llm_eval/inference/gemini_engine.py` - different pattern from anthropic
- `tests/unit/test_anthropic_engine.py` - test consolidation
