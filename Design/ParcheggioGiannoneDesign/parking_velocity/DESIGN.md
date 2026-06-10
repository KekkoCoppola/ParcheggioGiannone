---
name: Parking Velocity
colors:
  surface: '#f8f9ff'
  surface-dim: '#cbdbf5'
  surface-bright: '#f8f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eff4ff'
  surface-container: '#e5eeff'
  surface-container-high: '#dce9ff'
  surface-container-highest: '#d3e4fe'
  on-surface: '#0b1c30'
  on-surface-variant: '#434655'
  inverse-surface: '#213145'
  inverse-on-surface: '#eaf1ff'
  outline: '#737686'
  outline-variant: '#c3c6d7'
  surface-tint: '#0053db'
  primary: '#004ac6'
  on-primary: '#ffffff'
  primary-container: '#2563eb'
  on-primary-container: '#eeefff'
  inverse-primary: '#b4c5ff'
  secondary: '#565e74'
  on-secondary: '#ffffff'
  secondary-container: '#dae2fd'
  on-secondary-container: '#5c647a'
  tertiary: '#006242'
  on-tertiary: '#ffffff'
  tertiary-container: '#007d55'
  on-tertiary-container: '#bdffdb'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#dbe1ff'
  primary-fixed-dim: '#b4c5ff'
  on-primary-fixed: '#00174b'
  on-primary-fixed-variant: '#003ea8'
  secondary-fixed: '#dae2fd'
  secondary-fixed-dim: '#bec6e0'
  on-secondary-fixed: '#131b2e'
  on-secondary-fixed-variant: '#3f465c'
  tertiary-fixed: '#6ffbbe'
  tertiary-fixed-dim: '#4edea3'
  on-tertiary-fixed: '#002113'
  on-tertiary-fixed-variant: '#005236'
  background: '#f8f9ff'
  on-background: '#0b1c30'
  surface-variant: '#d3e4fe'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.02em
  display-md:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-lg:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  headline-sm:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
  label-sm:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '600'
    lineHeight: 12px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 4px
  unit-1: 4px
  unit-2: 8px
  unit-3: 12px
  unit-4: 16px
  unit-6: 24px
  unit-8: 32px
  unit-12: 48px
  margin-mobile: 16px
  margin-desktop: 32px
  gutter: 16px
---

## Brand & Style

The brand personality for this design system is **Efficient, Precise, and Trustworthy**. Designed for the high-stakes environment of urban transit and asset management, the UI prioritizes clarity and speed of interaction. The emotional response should be one of "calm control"—the user should feel that their vehicle and data are secure and that the path to their destination is clear.

The design style follows a **Modern Corporate** aesthetic with **Minimalist** influences. It utilizes generous whitespace, a strict alignment to a 4px baseline grid, and a sophisticated layering system. While the core is functional, subtle high-end details—such as soft depth and micro-interactions—elevate the experience from a utility tool to a premium management platform.

## Colors

The palette is anchored in **Deep Navy (#0F172A)** and **Slate Greys**, providing a professional foundation that suggests stability. The **Electric Blue (#2563EB)** serves as the primary driver for interaction, drawing the eye to conversion points and active states.

An **Emerald Green (#10B981)** accent is reserved for positive statuses, such as "Available" or "Payment Success," providing a clear semantic signal. Backgrounds are kept slightly off-white to reduce glare during outdoor usage, while surfaces use pure white to define distinct interactive zones.

## Typography

This design system utilizes **Inter** for all typographic needs. Its high x-height and neutral character make it exceptionally readable on mobile devices, even in variable lighting conditions typical of parking garages.

The scale is intentionally tight. **Display** levels use heavy weights and negative letter-spacing for a modern, impactful look. **Body** text remains at a minimum of 14px for accessibility. **Labels** utilize medium weights and subtle uppercase styling to differentiate metadata from primary content without increasing font size.

## Layout & Spacing

The system employs a **Fluid-Fixed Hybrid** grid. On mobile, the layout is fluid with 16px side margins. On desktop, content is contained within a 12-column grid with a maximum width of 1280px.

A strict **8px spacing rhythm** governs the hierarchy. Small components (chips, badges) use 4px or 8px increments, while larger layout sections (card-to-card spacing) use 24px or 32px to create clear visual breathing room. Breakpoints are defined at 640px (Tablet) and 1024px (Desktop).

## Elevation & Depth

Visual hierarchy is established through **Tonal Layers** and **Ambient Shadows**. This design system avoids harsh borders in favor of soft depth cues.

1.  **Level 0 (Background):** Slate-50, the canvas for all content.
2.  **Level 1 (Surface):** Pure White cards with a very soft, diffused shadow (Blur: 8px, Y: 2px, Opacity: 4% Black).
3.  **Level 2 (Active/Hover):** Increased shadow depth (Blur: 16px, Y: 4px, Opacity: 8% Black) to indicate interactivity.
4.  **Level 3 (Modals/Overlays):** High-diffusion shadows with a 10% backdrop blur (Glassmorphism) to maintain context of the underlying map or dashboard.

Low-contrast outlines (1px Slate-200) are used specifically for input fields and secondary buttons to maintain structure without clutter.

## Shapes

The shape language is defined by **Contemporary Roundedness**. Following the "2xl" mobile trend, primary containers (cards, modals) use a 1.5rem (24px) corner radius to feel approachable and modern.

Smaller elements like buttons and input fields utilize a 0.5rem (8px) radius to maintain a professional, structured appearance. Status indicators and notification badges should be fully pill-shaped (999px) to distinguish them from actionable buttons.

## Components

### Buttons
- **Primary:** Electric Blue background, white text, 8px radius. Heavy weight label.
- **Secondary:** White background, 1px Slate-200 border, Deep Navy text.
- **Ghost:** No background or border, Blue text. Used for low-priority actions.

### Cards
- White background, 24px radius, subtle Level 1 shadow. 
- Padding: 20px internal padding.
- Used for parking lot listings, payment methods, and user profiles.

### Inputs
- 1px Slate-200 border, 8px radius. 
- Focus state: 2px Electric Blue border with a 4px soft blue glow.
- Left-aligned icons are used for search and location inputs.

### Status Chips
- Used for "Available", "Full", or "Reserved".
- Semi-transparent background (10% opacity of the status color) with a solid text color for high contrast and modern feel.

### Map Pins
- Custom Electric Blue teardrop shapes with a white inner circle. Active pins should scale up by 15% to provide immediate visual feedback.