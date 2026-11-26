# SoloCrew — Frontend Design System

Status: Foundation established
Current default theme: midnight-botanical
Theme selection: Not implemented

## 1. Purpose

SoloCrew must support multiple curated visual themes in the future
without requiring component-by-component redesign.

The frontend therefore uses semantic design tokens as the contract
between UI components and the visual theme.

Components describe the role of a colour, not the colour itself.

For example, a primary action uses the brand-primary token.
It must not depend on a hardcoded hex value.

## 2. Current implementation

The Owner Web application defines its theme identity on the root
HTML element:

    <html data-theme="midnight-botanical">

The semantic token definitions are in:

    apps/owner-web/src/app/design-tokens.css

The token stylesheet is imported after the existing global,
authentication and brand styles so that compatibility aliases
can override earlier root-level colour declarations.

The current palette is Midnight Botanical.

## 3. Token naming conventions

New components should use semantic tokens.

### Canvas and surfaces

- `--sc-color-canvas`
- `--sc-color-surface`
- `--sc-color-surface-subtle`
- `--sc-color-surface-raised`
- `--sc-color-surface-selected`
- `--sc-color-surface-hover`

### Text

- `--sc-color-text-primary`
- `--sc-color-text-secondary`
- `--sc-color-text-muted`
- `--sc-color-text-subtle`
- `--sc-color-text-inverse`

### Brand and actions

- `--sc-color-brand-primary`
- `--sc-color-brand-primary-hover`
- `--sc-color-brand-on-primary`
- `--sc-color-brand-secondary`
- `--sc-color-brand-highlight`
- `--sc-color-brand-on-highlight`

### Navigation

- `--sc-color-navigation-background`
- `--sc-color-navigation-text`
- `--sc-color-navigation-muted`
- `--sc-color-navigation-hover`
- `--sc-color-navigation-active`
- `--sc-color-navigation-border`

### Borders and focus

- `--sc-color-border`
- `--sc-color-border-strong`
- `--sc-color-border-subtle`
- `--sc-color-focus-ring`

### Feedback

- `--sc-color-success`
- `--sc-color-success-surface`
- `--sc-color-success-border`
- `--sc-color-warning`
- `--sc-color-warning-surface`
- `--sc-color-warning-border`
- `--sc-color-danger`
- `--sc-color-danger-surface`
- `--sc-color-danger-border`
- `--sc-color-info`
- `--sc-color-info-surface`
- `--sc-color-info-border`

## 4. Component implementation rules

### Required

- Use semantic CSS variables for colour values.
- Use the same token for the same semantic purpose across modules.
- Keep component styles independent of a specific palette.
- Use semantic feedback tokens for success, warning, danger and
  informational states.
- Preserve visible focus indicators.
- Do not communicate status through colour alone.

### Not allowed in new reusable components

Avoid hardcoded palette values such as:

    color: #172d28;
    background: #527b64;
    border-color: #e4e6dc;

Use:

    color: var(--sc-color-text-primary);
    background: var(--sc-color-surface);
    border-color: var(--sc-color-border);

Do not introduce inline brand colours in React components or SVGs.

### Exceptions

Fixed colours may be appropriate for content-specific assets,
such as an illustration or a product image. They must not become
a substitute for the semantic UI theme.

## 5. Existing CSS migration

The current application contains styles created before this
token foundation.

Temporary compatibility aliases are provided in
`design-tokens.css`, including:

- `--page-background`
- `--surface`
- `--sidebar-background`
- `--ink`
- `--accent`
- `--border`
- `--warning-background`

These aliases exist only to preserve the existing UI during
migration.

New components must not use these legacy aliases.

As existing modules are changed, migrate their reusable colour
styles to the `--sc-color-*` token contract.

Do not expose theme selection to users until the remaining
legacy hardcoded palette values have been reviewed and migrated.

## 6. Future theme architecture

The root `data-theme` attribute is the theme selection boundary.

Future theme definitions will provide values for the same semantic
tokens. Components will not need theme-specific variants.

Illustrative future theme identities:

- `midnight-botanical`
- `deep-atlantic`
- `indigo-electric`
- `slate-terracotta`

The names and final palette values are subject to future design
review.

Theme selection itself is outside the scope of the current
foundation.

## 7. Future preference ownership

Theme preference and workspace branding are separate concepts.

Planned ownership:

- Personal theme preference belongs to the user.
- Business identity and branding belong to the workspace.
- Platform default theme belongs to SoloCrew.

A user's personal visual preference must not silently change the
experience of other members of the same workspace.

When user preferences are introduced, persistence should be
server-side so the selection can follow the user across devices
and be applied during server rendering.

No database migration or user preference API is introduced by
this foundation.

## 8. Accessibility

Every future theme must be reviewed for:

- Readable text and control contrast.
- Visible keyboard focus.
- Readable disabled states.
- Distinguishable success, warning and error states.
- Status indicators that do not rely on colour alone.
- Reduced-motion support where animations are used.

A theme is not eligible for release merely because its colours
look visually consistent.

## 9. Future milestone

SC-UX-APPEARANCE-001 — Workspace Appearance & Personalization

Planned scope:

- Curated palette library.
- Theme previews.
- User theme selection.
- Persistent personal theme preference.
- Server-side theme resolution.
- Accessibility validation.
- Consistent theme application across Owner Web.

Workspace-specific branding remains a separate future capability.
