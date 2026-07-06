# Security Policy

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 2.1.x   | :white_check_mark: |
| < 2.1   | :x:                |

The latest 2.1.x release receives security fixes. Older versions are not
maintained once a newer release is available.

## Reporting a Vulnerability

Please do not report security vulnerabilities through public GitHub issues.

Instead, use one of these channels:

- **GitHub private vulnerability reporting** (preferred):
  [Report a vulnerability](https://github.com/bassrehab/smpp-kafka-producer/security/advisories/new)
- **Email**: contact@smppgateway.io with subject line `[SECURITY] smpp-kafka-producer`

Include as much of the following as you can: affected version, a description
of the issue and its impact, and steps or a proof-of-concept to reproduce it.

## What to Expect

- **Acknowledgement** of your report within 72 hours.
- **Assessment and triage** within 7 days, including a severity estimate.
- **A fix or mitigation plan** for confirmed vulnerabilities, targeted at the
  next patch release. Credit is given to reporters unless anonymity is
  requested.

Please allow us reasonable time to release a fix before public disclosure.

## Scope

smpp-kafka-producer is a network-facing service (SMPP server + HTTP/2 REST
API). Authentication bypasses, PDU parsing issues, HTTP endpoint
vulnerabilities, and container/deployment hardening gaps are all in scope.
Dependency vulnerabilities are tracked via Dependabot and addressed in patch
releases.
