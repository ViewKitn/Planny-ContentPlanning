---
name: Planny
description: A calm Thai workspace for a creator's shared content lifecycle.
colors:
  primary: "#215f89"
  primary-dark: "#174c70"
  canvas: "#f3f7fa"
  sidebar: "#e8f1f7"
  surface: "#fff"
  ink: "#18374b"
  muted: "#597082"
  line: "#dce6ed"
  field-border: "#cbd9e3"
  focus: "#277fb0"
  field-focus-ring: "#c7e3f5"
  action-focus-ring: "#77b9df"
  idea: "#567690"
  idea-soft: "#e4edf5"
  draft: "#765199"
  draft-soft: "#eee7f5"
  ready: "#2b7b7c"
  ready-soft: "#e0f0ee"
  planned: "#996220"
  planned-soft: "#f7ecd9"
  published: "#2f7655"
  published-soft: "#e4f1e8"
  danger: "#a33e36"
  danger-hover: "#8b302a"
  card-border: "#d8e3eb"
  card-hover-border: "#79a9c9"
  card-hover: "#fbfdff"
  button-hover: "#edf4f9"
  nav-text: "#456278"
  nav-hover: "#d8e8f2"
  nav-hover-text: "#143e5d"
typography:
  headline:
    fontFamily: "'Noto Sans Thai', sans-serif"
    fontSize: "28px"
    fontWeight: 600
    lineHeight: 1.4
    letterSpacing: "-.025em"
  title:
    fontFamily: "'Noto Sans Thai', sans-serif"
    fontSize: "17px"
    fontWeight: 600
    lineHeight: 1.5
  body:
    fontFamily: "'Noto Sans Thai', sans-serif"
    fontSize: "14px"
    lineHeight: 1.65
  label:
    fontFamily: "'Noto Sans Thai', sans-serif"
    fontSize: "12px"
  stage:
    fontFamily: "Manrope, sans-serif"
    fontSize: "14px"
    fontWeight: 700
    lineHeight: 1.5
  wordmark:
    fontFamily: "Manrope, sans-serif"
    fontSize: "32px"
    lineHeight: 1
    letterSpacing: "-.04em"
  writing:
    fontFamily: "'Noto Sans Thai', sans-serif"
    fontSize: "14px"
    lineHeight: 1.85
rounded:
  chip: "4px"
  badge: "5px"
  compact: "6px"
  control: "8px"
  nav: "9px"
  toast: "10px"
  card: "12px"
  writing: "14px"
  dialog: "16px"
spacing:
  xs: "4px"
  sm: "8px"
  card-gap: "12px"
  board-gap: "16px"
  md: "20px"
  lg: "24px"
  writing: "30px"
  page-x: "32px"
  page-y: "34px"
  wide-page: "40px"
components:
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.surface}"
    rounded: "{rounded.control}"
    padding: "9px 14px"
  button-primary-hover:
    backgroundColor: "{colors.primary-dark}"
  button-secondary:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.ink}"
    rounded: "{rounded.control}"
    padding: "9px 14px"
  button-secondary-hover:
    backgroundColor: "{colors.button-hover}"
  button-subtle:
    backgroundColor: "transparent"
    textColor: "{colors.nav-text}"
    rounded: "{rounded.control}"
    padding: "9px 14px"
  button-danger:
    backgroundColor: "{colors.danger}"
    textColor: "{colors.surface}"
    rounded: "{rounded.control}"
    padding: "9px 14px"
  button-danger-hover:
    backgroundColor: "{colors.danger-hover}"
  input:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.ink}"
    rounded: "{rounded.control}"
    padding: "9px 12px"
  nav-item:
    textColor: "{colors.nav-text}"
    rounded: "{rounded.nav}"
    padding: "11px 13px"
  nav-item-selected:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.surface}"
  content-card:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.ink}"
    rounded: "{rounded.card}"
    padding: "16px 14px 12px"
  writing-surface:
    backgroundColor: "{colors.surface}"
    rounded: "{rounded.writing}"
    padding: "{spacing.writing}"
  status-idea:
    backgroundColor: "{colors.idea-soft}"
    textColor: "{colors.idea}"
    rounded: "{rounded.badge}"
    padding: "3px 8px"
  status-draft:
    backgroundColor: "{colors.draft-soft}"
    textColor: "{colors.draft}"
    rounded: "{rounded.badge}"
    padding: "3px 8px"
  status-ready:
    backgroundColor: "{colors.ready-soft}"
    textColor: "{colors.ready}"
    rounded: "{rounded.badge}"
    padding: "3px 8px"
  status-planned:
    backgroundColor: "{colors.planned-soft}"
    textColor: "{colors.planned}"
    rounded: "{rounded.badge}"
    padding: "3px 8px"
  status-published:
    backgroundColor: "{colors.published-soft}"
    textColor: "{colors.published}"
    rounded: "{rounded.badge}"
    padding: "3px 8px"
---

# Design System: Planny

## Overview

**Creative North Star: "Glacier workspace"**

Planny is a Thai, Light Mode, Modern SaaS workspace with a glacier blue canvas, maritime blue navigation and white work surfaces. Its character is calm, readable and practical: compact controls organize content while the editor gives writing generous space.

Noto Sans Thai carries interface text and prose. Manrope distinguishes the wordmark and English lifecycle labels. Color helps recognize the shared lifecycle, while visible names and Thai explanations make each stage understandable.

**Key Characteristics:**
- Thai text with comfortable line height.
- Pale blue surroundings and white work surfaces.
- Restrained borders and rounded controls.
- Five labeled lifecycle colors.
- Desktop first, with horizontally scrollable board lanes on mobile.

## Colors

The frontmatter records the reused source values from `frontend/src/style.css`; stage pairs are semantic colors rather than a second brand palette.

### Primary

- **Maritime blue** (`primary`): primary actions, selected navigation, links and input carets. `primary-dark` supplies primary hover and the wordmark.
- **Focus blue** (`focus`): focused field borders, with separate pale field and stronger action rings.

### Secondary

- **Muted slate blue** (`idea`, `idea-soft`): Idea stage marker, count and badge.
- **Soft violet** (`draft`, `draft-soft`): Draft stage marker, count and badge.
- **Muted teal** (`ready`, `ready-soft`): Ready stage marker, count and badge.
- **Warm amber** (`planned`, `planned-soft`): Planned stage marker, count and badge.
- **Leaf green** (`published`, `published-soft`): Published stage marker, count and badge.
- **Brick red** (`danger`, `danger-hover`): destructive confirmation and destructive action text.

### Neutral

- **Glacier canvas** (`canvas`): application background.
- **Blue mist** (`sidebar`): navigation surface.
- **White** (`surface`): controls, cards, writing surface and dialogs.
- **Deep blue ink** (`ink`): default text. `muted` is supporting prose.
- **Cool divider** (`line`): main surface boundaries and separators; `field-border` and `card-border` distinguish interactive surfaces.

**The Labeled Stage Rule.** Pair lifecycle color with its visible English name; use Thai explanation where the board or selector provides context.

## Typography

The interface inherits Noto Sans Thai with a sans-serif fallback. Manrope with a sans-serif fallback carries the wordmark and stage headings; status badges use Manrope. Keep Thai labels in their normal case.

The headline, title, body, stage, wordmark and writing roles are recorded above. Smaller metadata ranges from 10–13px in cards, filters and helpers; form labels use the label role. Third-level headings use 15px at weight 600. Card titles use 14px with a 1.7 line height. The editor title uses 27px at weight 600 and a 1.5 line height.

At the mobile breakpoint, page headlines and editor titles become 23px. Rich writing retains the writing line height, with 18px internal padding and a minimum editing height of 140px.

## Layout

The fixed desktop navigation is 224px wide. Main content offsets by the same width. A 64px top bar separates application context from the work area. Standard page padding is 34px vertically at the top and 32px horizontally, with 40px at the bottom.

The board uses five equal columns, each at least 195px wide, with a 16px gap and horizontal overflow. Column cards have 12px gaps. Filters form a horizontal row that wraps at narrower widths; search grows within a 420px desktop maximum.

The editor uses a flexible writing column and a 286px inspector with a 30px gap. The white writing surface has 30px padding; writing sections separate by 34px. The inspector is divided by fine lines rather than enclosing every section in a card. Settings use two columns within a 1000px maximum. The calendar preserves seven columns within a 700px minimum grid and scrolls horizontally when needed.

At widths of at least 1550px, page padding becomes 40px, board gaps 20px, writing padding 36px and the inspector 300px. At widths up to 1100px, navigation becomes 190px, page horizontal padding 22px, writing padding 22px and the inspector 245px with a 20px gap.

At widths up to 760px, navigation becomes an off-canvas 224px panel and the top bar becomes 56px. Page padding is 24px 16px. Search occupies a whole filter row. Board lanes remain five 220px columns with 14px gaps. The editor becomes one column, with inspector sections in a two-column grid; settings become one column. The writing surface uses 20px 16px padding.

## Elevation & Depth

The workspace uses tonal layering and fine borders at rest. Cards have no shadow and hover by changing border and background. Shadows are reserved for floating feedback, dialogs and the mobile navigation panel.

- **Toast:** `0 8px 24px #183f5826`.
- **Dialog:** `0 14px 50px #0b25352e`.
- **Mobile navigation:** `8px 0 32px #123b5321`.
- **Title field focus:** `0 2px 0 #b8d6e8`.
- **Rich editor focus:** `inset 0 0 0 2px #b8d9ed`.

The dialog overlay uses `#14344c70`. Focus shadows are field cues, not resting elevation.

## Shapes

Controls use gently curved corners; cards are more rounded and the writing surface and dialog more generous. The frontmatter records the observed radius vocabulary. Small stage counts and badges use compact corners, while dots are circular. Empty lanes use dashed borders and the same radius as cards. Borders are generally 1px; lane headers have a 2px pale stage-colored bottom border.

## Components

### Platform identity and page profile

Platform labels use locally bundled Simple Icons SVGs for Facebook, Instagram, TikTok and YouTube in their supplied brand colors. Keep the readable platform name beside each logo; unknown platform names use the existing Globe icon language. Logos are decorative when a name is already present and never loaded from user-supplied image URLs.

The page profile extends the existing flat workspace: a personal introduction beside a vertical list of platforms on desktop, stacked on narrow screens. Each platform has a logo, labeled page URL input, explicit save feedback and an “open page” link once a valid URL is saved. Links open a new tab; inactive platforms retain their page links. Use the existing field, button, line, focus and responsive tokens.

### Buttons

Primary buttons are compact maritime blue actions with white labels; neutral buttons have a white ground and field-colored border. Both use 9px 14px padding, weight 500, a 1.5 line height and a 7px icon gap. Small buttons use 12px type and 6px 10px padding. Subtle buttons use a transparent border and ground, with pale blue hover. Destructive confirmations use brick red; the editor's trash action uses destructive text on a neutral button.

All buttons shift down 1px while active. Buttons and links receive a 3px action focus ring with a 3px offset. Disabled buttons use 0.55 opacity and a wait cursor. Icons are Lucide SVGs, usually 14–18px in controls.

### Chips

Platform and tag labels inside cards use pale blue fills, 10px text and compact corners. Selectable inspector tags have a white background, 1px border and 6px corners; checked tags use `#dcebf5` with a `#86b1cc` border. Lifecycle badges use their stage color pair, Manrope at 11px and 3px 8px padding.

### Cards / Containers

Content cards are white, bordered, full-width buttons with left-aligned text. Hover changes to `card-hover` and `card-hover-border` over 0.16s using CSS default ease; focus follows the action ring. Titles wrap and brief excerpts clamp to two lines. A light footer divider separates timing metadata. Empty lanes use centered icon and copy rather than an enclosing shadow.

Writing surfaces use white, the main divider and 14px corners. List and calendar containers use white, the same divider and 12px corners. Dialogs use 16px corners, 28px padding and a 460px width capped by the viewport; mobile dialog padding is 24px.

### Inputs / Fields

Fields use a white ground, 1px field border, 8px corners and 9px 12px padding. Focus changes the border to focus blue with a 2px field ring and 1px outline offset. The search wrapper owns the focus outline; its child input does not add a second ring. Textareas resize vertically. Title editing uses an unbordered field with an underline-like focus shadow.

Rich editors use a 9px enclosing radius and a pale toolbar. Toolbar buttons display a pale blue background for hover or pressed state. The content area uses the writing typography and inset focus cue. Error feedback appears in a pale warm bordered banner; the implementation does not define a separate generic error-field skin.

### Navigation

Navigation rows have 9px corners, 11px 13px padding, 500 weight and an 11px icon gap. Selected rows use the primary color and white text. Unselected hover uses `nav-hover` and `nav-hover-text`. Mobile navigation slides from the left over 0.2s with default CSS ease.

### Lifecycle track

The inspector track has five equal 4px-high segments with 3px corners and 4px gaps. Unreached segments are `#d7e3eb`; reached segments are `#4385ae`. Background changes transition over 0.22s with default CSS ease. This track supplements the labeled status selector.

Motion is limited to these state transitions and a 1s linear repeating loading rotation. The stylesheet removes animations and transitions and restores automatic scrolling when reduced motion is requested.

## Do's and Don'ts

### Do:

- Do preserve Noto Sans Thai for Thai text and its comfortable line height.
- Do use maritime blue for primary actions and selected navigation.
- Do keep lifecycle names visible alongside their colors.
- Do give writing more space than supporting controls.
- Do preserve visible keyboard focus and the reduced-motion override.

### Don't:

- Don't replace the confirmed Light Mode glacier canvas with a dark interface.
- Don't use color alone to communicate lifecycle status.
- Don't add resting shadows to every content card.
- Don't squeeze the five mobile board lanes into illegible narrow columns.
