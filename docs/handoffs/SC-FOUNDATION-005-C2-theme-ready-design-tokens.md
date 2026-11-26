# SC-FOUNDATION-005-C2 — Theme-ready Design Token Foundation

- Status: Implemented; validation pending
- Parent slice: SC-FOUNDATION-005-C2 — Owner Web Authentication & Access
- Branch: feature/SC-FOUNDATION-005-identity-owner-access

## Objective

Prepare Owner Web for future user-selectable visual themes without
implementing theme selection or preference persistence now.

## Delivered in this substep

- Central semantic design-token stylesheet.
- Explicit root theme identity using `data-theme`.
- Midnight Botanical retained as the current default theme.
- Semantic tokens for:
  - Canvas and surfaces.
  - Text hierarchy.
  - Brand colours and actions.
  - Navigation.
  - Borders and focus.
  - Success, warning, danger and information feedback.
- Temporary compatibility aliases for existing CSS variables.
- Design-system documentation.
- Rules requiring new reusable UI components to consume semantic
  tokens instead of hardcoded palette values.

## Important locations

    apps/owner-web/src/app/design-tokens.css
    apps/owner-web/src/app/layout.tsx
    docs/development/design-system.md

## Architecture decisions

- Theme identity is applied at the HTML root.
- Components consume semantic tokens rather than theme-specific
  values.
- The current theme is `midnight-botanical`.
- Legacy colour aliases remain temporarily to avoid breaking
  existing styles during migration.
- New components must not use legacy aliases.
- Existing CSS will be progressively migrated before theme
  selection is exposed to users.

## Explicitly out of scope

- Theme selector UI.
- Live theme preview.
- User preference API.
- Database persistence of theme selection.
- Workspace-specific custom branding.
- Dark mode.

## Future milestone

SC-UX-APPEARANCE-001 — Workspace Appearance & Personalization.

Planned capabilities:

- Curated palette library.
- Live theme previews.
- Personal theme selection.
- Server-side preference persistence.
- Consistent theme application across Owner Web.
- Accessibility validation.

## Validation

Pending local validation of the design-token stylesheet,
application build and existing Owner Web screens.
