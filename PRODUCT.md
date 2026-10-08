# Planny

<!-- impeccable:product-schema 1 -->

## Platform

web

## Stack

Vue 3 + Spring Boot + PostgreSQL, confirmed by the user.

## Users

A solo Thai content creator managing one page across several platforms.

## Product Purpose

Manage ideas, writing, publication planning and manually recorded publication in one local tool.

## Operating Context

Desktop localhost application without accounts. A content item uses a shared lifecycle and publication time across platforms; captions and links are separate per platform.

## Capabilities and Constraints

Confirmed specification: docs/planny-spec.md. Board/list, month/week calendar, editor, tags, archive/trash, settings and backup. PostgreSQL persistence, autosave and local recovery drafts. No AI, automatic posting, uploads, brands or team features in this version.

Platform logos identify destinations. A page profile stores one page URL per platform and opens that configured page in a new tab; these URLs are distinct from individual post links.

## Brand Commitments

Planny; Thai interface, English lifecycle names with Thai explanations, Modern SaaS, Light Mode, readable text and a generous editor. Desktop first.

## Evidence on Hand

Confirmed interviews and ADRs in docs/. No user content or visual assets supplied; the application starts empty.

## Product Principles

- Complete the core lifecycle before integrations.
- One content item represents the creator's publication across platforms.
- Keep planned and actual publication distinct.
- Preserve recoverable work and historical data.
