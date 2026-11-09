# Online Supermarket System - Human-Machine Interface (HMI) Design Document

> [简体中文](../zh/hmi-design.md) | English

> This document is intended for the development team. It defines the human-machine interface design specification of the Online Supermarket System, including interface prototypes, interaction flows, component specifications, responsive design, accessibility design, motion descriptions, and other content.

---

## I. Global Design Specification

### 1.1 Visual Design Specification

#### Color System

| Color Type | Color Value | Purpose |
|------------|-------------|---------|
| Primary color | `#E2231A` | Buttons, links, highlighted states, prices (JD/Taobao red) |
| Primary light | `#FF4D4F` | Primary hover state |
| Primary dark | `#C41D15` | Primary pressed state |
| Success color | `#52C41A` | Success messages, Listed status |
| Warning color | `#FAAD14` | Warning messages, low stock |
| Danger color | `#FF4D4F` | Delete buttons, error messages, refund status |
| Info color | `#1890FF` | Secondary information, links |
| Primary text color | `#333333` | Headings, body text |
| Secondary text color | `#666666` | Descriptive text |
| Auxiliary text color | `#999999` | Placeholders, timestamps |
| Border color | `#D9D9D9` | Dividers, borders |
| Background color | `#F5F5F5` | Page background |
| White | `#FFFFFF` | Card background, input fields |

#### Typography Specification

| Level | Font Size | Font Weight | Line Height | Purpose |
|-------|-----------|-------------|-------------|---------|
| H1 | 24px | 600 | 32px | Page title |
| H2 | 20px | 600 | 28px | Section title |
| H3 | 16px | 600 | 24px | Card title, table header |
| Body | 14px | 400 | 20px | Regular text, form labels |
| Auxiliary text | 12px | 400 | 18px | Hint text, timestamps, tags |
| Price | 18px | 700 | 24px | Product price display |

#### Spacing System

An **8px baseline grid system** is used, and all spacing values are multiples of 8:

| Spacing Variable | Value | Purpose |
|------------------|-------|---------|
| `--spacing-xs` | 4px | Icon-to-text spacing |
| `--spacing-sm` | 8px | Element padding, compact spacing |
| `--spacing-md` | 12px | Compact card padding |
| `--spacing-base` | 16px | Card padding, regular spacing |
| `--spacing-lg` | 24px | Section spacing, form spacing |
| `--spacing-xl` | 32px | Page block spacing |
| `--spacing-xxl` | 48px | Large block separation |

#### Border Radius Specification

| Type | Value | Purpose |
|------|-------|---------|
| Small radius | 4px | Buttons, tags, input fields |
| Medium radius | 8px | Cards, dialogs |
| Large radius | 12px | Image containers |
| Full radius | 50% | Avatars, circular icons |

#### Shadow Specification

| Level | CSS Variable | Value | Purpose |
|-------|--------------|-------|---------|
| No shadow | — | `none` | Default state |
| Light shadow | `--shadow-sm` | `0 1px 2px 0 rgba(0,0,0,0.05)` | Card borders, light hover |
| Base shadow | `--shadow-base` | `0 2px 8px rgba(0,0,0,0.08)` | Card hover, dropdown menus |
| Medium shadow | `--shadow-lg` | `0 4px 16px rgba(0,0,0,0.12)` | Dialogs, drawers |
| Deep shadow | `--shadow-xl` | `0 8px 24px rgba(0,0,0,0.15)` | Modal dialogs, full-screen panels |

#### Design Tokens (CSS Variable System)

All visual specifications are uniformly defined as CSS variables in `src/styles/variables.css`. Component styles reference variables rather than hard-coded color values, which facilitates theme switching and unified maintenance. Core variable groups:

```css
:root {
  /* Primary colors */
  --color-primary: #e2231a;
  --color-primary-light: #ff4d4f;
  --color-primary-dark: #c41d15;
  --color-primary-hover: #f53b32;

  /* Auxiliary colors */
  --color-success: #52c41a;
  --color-warning: #faad14;
  --color-danger: #ff4d4f;
  --color-info: #1890ff;

  /* Text colors */
  --text-primary: #333333;
  --text-regular: #666666;
  --text-secondary: #999999;
  --text-placeholder: #cccccc;
  --text-white: #ffffff;

  /* Background colors */
  --bg-primary: #ffffff;
  --bg-secondary: #f5f5f5;
  --bg-tertiary: #fafafa;
  --bg-hover: #f0f0f0;

  /* Border colors */
  --border-light: #f0f0f0;
  --border-base: #d9d9d9;
  --border-dark: #bfbfbf;

  /* Price colors */
  --price-primary: #e2231a;
  --price-secondary: #ff6b6b;

  /* Spacing (8px baseline) */
  --spacing-xs: 4px;
  --spacing-sm: 8px;
  --spacing-md: 12px;
  --spacing-base: 16px;
  --spacing-lg: 24px;
  --spacing-xl: 32px;
  --spacing-xxl: 48px;

  /* Border radius */
  --radius-sm: 4px;
  --radius-base: 8px;
  --radius-lg: 12px;
  --radius-xl: 16px;
  --radius-round: 999px;

  /* Shadows */
  --shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  --shadow-base: 0 2px 8px rgba(0, 0, 0, 0.08);
  --shadow-lg: 0 4px 16px rgba(0, 0, 0, 0.12);
  --shadow-xl: 0 8px 24px rgba(0, 0, 0, 0.15);

  /* Font */
  --font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;

  /* Layout */
  --header-height: 60px;
  --content-width: 1200px;
  --sidebar-width: 240px;

  /* Transitions */
  --transition-fast: 0.15s ease;
  --transition-base: 0.25s ease;
  --transition-slow: 0.35s ease;
}
```

### 1.2 Component Naming Convention

| Type | Naming Rule | Example |
|------|-------------|---------|
| Page component | `XxxView.vue` | `HomeView.vue`, `CartView.vue` |
| Layout component | `XxxLayout.vue` | `MallLayout.vue`, `MerchantLayout.vue` |
| Common component | `PascalCase` | `ProductCard.vue`, `SearchBar.vue` |
| Business component | `Business prefix + feature` | `PaymentCard.vue`, `ImageUpload.vue` |
| Composable function | `useXxx.ts` | `useWebSocket.ts`, `usePagination.ts` |

### 1.3 Element Plus Usage Guidelines

| Scenario | Recommended Component | Notes |
|----------|-----------------------|-------|
| Form input | `el-form` + `el-form-item` | Always use label-position="top" |
| Button | `el-button` | Use type="primary" for primary actions and type="danger" for dangerous actions |
| Table | `el-table` | Must include border and stripe |
| Pagination | `el-pagination` | Always use layout="total, sizes, prev, pager, next, jumper" |
| Dialog | `el-dialog` | Use a uniform width of 600px and right-align action buttons |
| Message | `ElMessage` | Use success for success, error for errors, and warning for warnings |
| Confirmation dialog | `ElMessageBox.confirm` | Dangerous operations such as deletion require secondary confirmation |
| Loading state | `v-loading` directive | Display while a table/card is loading |
| Empty state | `el-empty` | Display when the list is empty |
| Tag | `el-tag` | Use different types to display statuses |
| Breadcrumb | `el-breadcrumb` | Display at the top of admin backend pages |

### 1.4 Props/Events Conventions

#### Props Definition Convention

```typescript
// Props must be defined using a TypeScript interface
interface ProductCardProps {
  /** Product data */
  product: Product
  /** Whether to show the add-to-cart button */
  showAddCart?: boolean
  /** Card width */
  width?: string
}

// A generic type must be specified when using defineProps
const props = defineProps<ProductCardProps>()
```

#### Events Definition Convention

```typescript
// Types must be specified when using defineEmits
const emit = defineEmits<{
  /** Add-to-cart event */
  (e: 'add-to-cart', skuId: number, quantity: number): void
  /** Product card click event */
  (e: 'click', productId: number): void
}>()
```

---

## II. Responsive Design (Desktop)

### 2.1 Breakpoint Definitions

| Breakpoint | Width Range | Description |
|------------|-------------|-------------|
| Small desktop | 1200px - 1439px | Laptop screens |
| Medium desktop | 1440px - 1919px | Standard monitors |
| Large desktop | ≥1920px | High-resolution monitors |

### 2.2 Layout Adaptation Rules

| Component | Small Desktop | Medium Desktop | Large Desktop |
|-----------|---------------|----------------|---------------|
| Max content width | 1200px | 1440px | 1600px |
| Product list columns | 3 columns | 4 columns | 5 columns |
| Sidebar width (admin backend) | 200px | 220px | 240px |
| Table action column | Fixed 120px | Fixed 120px | Fixed 120px |

### 2.3 Responsive Implementation

```css
/* Implement responsive layout with CSS variables */
:root {
  --content-max-width: 1200px;
  --product-grid-columns: 3;
  --sidebar-width: 200px;
}

@media (min-width: 1440px) {
  :root {
    --content-max-width: 1440px;
    --product-grid-columns: 4;
    --sidebar-width: 220px;
  }
}

@media (min-width: 1920px) {
  :root {
    --content-max-width: 1600px;
    --product-grid-columns: 5;
    --sidebar-width: 240px;
  }
}
```

---

## III. Accessibility Design (Accessibility)

### 3.1 Keyboard Navigation Support

| Action | Shortcut | Description |
|--------|----------|-------------|
| Global search | `Ctrl + K` / `Cmd + K` | Focus the search box |
| Cart | `Ctrl + C` / `Cmd + C` | Navigate to the cart page |
| Tab navigation | `Tab` / `Shift+Tab` | Move focus forward/backward |
| Confirm action | `Enter` | Trigger a button/link |
| Cancel/Close | `Esc` | Close a dialog/dropdown menu |
| List selection | `↑` / `↓` | Navigate dropdown options |

### 3.2 Focus Management Guidelines

```css
/* Global focus style */
:focus-visible {
  outline: 2px solid #E2231A;
  outline-offset: 2px;
}

/* Input focus */
.el-input__inner:focus {
  border-color: #E2231A;
  box-shadow: 0 0 0 2px rgba(226, 35, 26, 0.2);
}

/* Button focus */
.el-button:focus-visible {
  outline: 2px solid #E2231A;
  outline-offset: 2px;
}
```

### 3.3 Color Contrast Requirements

| Combination | Contrast Ratio | WCAG Level |
|-------------|----------------|------------|
| Primary text `#333333` / white background | 14.2:1 | AAA |
| Secondary text `#666666` / white background | 7.0:1 | AAA |
| Primary color `#E2231A` / white background | 5.1:1 | AA |
| Danger color `#FF4D4F` / white background | 4.1:1 | AA |
| Auxiliary text `#999999` / white background | 2.9:1 | AA (large text) |

### 3.4 ARIA Attribute Usage Guidelines

| Scenario | ARIA Attribute | Example |
|----------|----------------|---------|
| Loading state | `aria-busy="true"` | While a table is loading data |
| Disabled state | `aria-disabled="true"` | When a button is not clickable |
| Expanded/Collapsed | `aria-expanded` | Dropdown menus, accordion panels |
| Dialog | `role="dialog"` + `aria-modal="true"` | Dialog component |
| Navigation | `role="navigation"` | Sidebar, top navigation |
| Main content | `role="main"` | Page content area |
| Form error | `aria-describedby` | Associate with error hint text |
| Live region | `aria-live="polite"` | Cart quantity update announcement |

### 3.5 Screen Reader Optimization

```html
<!-- Product card example -->
<article 
  :aria-label="`Product: ${product.name}, Price: ${product.price} CNY`"
  role="article"
>
  <h3>{{ product.name }}</h3>
  <p aria-label="Price">{{ product.price }} CNY</p>
  <button 
    aria-label="Add {{ product.name }} to cart"
    @click="addToCart"
  >
    Add to Cart
  </button>
</article>

<!-- Pagination component example -->
<nav aria-label="Product list pagination">
  <el-pagination ... />
</nav>

<!-- Cart quantity update -->
<div aria-live="polite" aria-atomic="true" class="sr-only">
  Cart item count: {{ cartCount }}
</div>
```

### 3.6 E-commerce/Supermarket-Specific Accessibility Requirements

| Requirement | Description |
|-------------|-------------|
| Price readability | Prices must include a currency unit and be readable by screen readers |
| Stock status | Must use both text and color indicators (e.g., a "Low Stock" tag) |
| Action feedback | Actions such as add to cart and place order must provide clear text feedback |
| Form errors | Form validation errors must be associated with the corresponding input field |
| Image alternative text | Product images must have an `alt` attribute describing the product name |
| Payment security notice | The payment page must display clear security notice text |

---

## IV. Motion Design Specification

### 4.1 Motion Principles

| Principle | Description |
|-----------|-------------|
| Fast response | All animations must not exceed 300ms |
| Natural and smooth | Use the `ease-out` easing function |
| Meaningful | Animations must serve an interaction purpose, not decoration |
| Dismissible | Respect the system-level "Reduce Motion" setting |

### 4.2 Animation Type Specification

| Animation Type | Duration | Easing Function | Purpose |
|----------------|----------|-----------------|---------|
| Fade | 200ms | `ease-out` | Dialog and message appearance/disappearance |
| Slide | 250ms | `ease-out` | Sidebar expand/collapse, dropdown menus |
| Scale feedback | 150ms | `ease-out` | Button click, card hover |
| Page transition | 300ms | `ease-in-out` | Route transition |
| Loading animation | Infinite loop | `linear` | Loading indicator |
| List item appearance | 200ms | `ease-out` | Product list load animation (50ms stagger) |
| Error shake | 400ms | `ease-in-out` | Form validation failure |
| Success feedback | 300ms | `ease-out` | Add-to-cart success icon animation |

### 4.3 CSS Motion Implementation

```css
/* Reduced motion preference */
@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}

/* Button click scale */
.btn-press {
  transition: transform 150ms ease-out;
}
.btn-press:active {
  transform: scale(0.95);
}

/* Card hover effect */
.card-hover {
  transition: transform 200ms ease-out, box-shadow 200ms ease-out;
}
.card-hover:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

/* Error shake animation */
@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-8px); }
  75% { transform: translateX(8px); }
}
.input-error {
  animation: shake 400ms ease-in-out;
}

/* Staggered list item appearance animation */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
.list-item {
  animation: fadeInUp 200ms ease-out both;
}
.list-item:nth-child(1) { animation-delay: 0ms; }
.list-item:nth-child(2) { animation-delay: 50ms; }
.list-item:nth-child(3) { animation-delay: 100ms; }
.list-item:nth-child(4) { animation-delay: 150ms; }
.list-item:nth-child(5) { animation-delay: 200ms; }
```

### 4.4 Vue Transition Usage Guidelines

```vue
<!-- Page transition -->
<router-view v-slot="{ Component }">
  <transition name="page-fade" mode="out-in">
    <component :is="Component" />
  </transition>
</router-view>

<style>
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 300ms ease-in-out;
}
.page-fade-enter-from,
.page-fade-leave-to {
  opacity: 0;
}
</style>

<!-- Dialog transition -->
<transition name="dialog-fade">
  <el-dialog v-model="visible">
    ...
  </el-dialog>
</transition>

<style>
.dialog-fade-enter-active,
.dialog-fade-leave-active {
  transition: opacity 200ms ease-out;
}
.dialog-fade-enter-from,
.dialog-fade-leave-to {
  opacity: 0;
}
</style>
```

---

## V. Functional Module Interface Design

### 5.1 User Authentication Module

#### 5.1.1 Sign-in Page

**ASCII prototype:**

```
┌────────────────────────────────────────────────────────────┐
│                                                            │
│                   ┌─────────────┐                          │
│                   │    LOGO     │                          │
│                   └─────────────┘                          │
│                                                            │
│             ┌────────────────────────────────┐             │
│             │   Online Supermarket Sign In   │             │
│             │                                │             │
│             │  Username/Email                │             │
│             │  [__________________________]  │             │
│             │                                │             │
│             │  Password                      │             │
│             │  [__________________________]  │             │
│             │                                │             │
│             │  ☐ Remember me                 │             │
│             │                                │             │
│             │  [          Sign In         ]  │             │
│             │                                │             │
│             │  Forgot password? | Register   │             │
│             └────────────────────────────────┘             │
│                                                            │
└────────────────────────────────────────────────────────────┘
```

**Interaction flow:**

```
User visits the sign-in page
    │
    ├─ Enter username/email
    │   └─ Validate format on blur
    │       ├─ Invalid format → show red error message
    │       └─ Valid format → clear error message
    │
    ├─ Enter password
    │   └─ Validate non-empty on blur
    │
    ├─ Click the "Sign In" button
    │   ├─ Button shows Loading state (disabled)
    │   ├─ Call the sign-in API
    │   │   ├─ Success → show success message → navigate to home page
    │   │   └─ Failure → show error message → re-enable the button
    │   └─ 5 consecutive failures → show the captcha input field
    │
    └─ Click "Forgot password" → navigate to the forgot-password page
    └─ Click "Register" → navigate to the registration page
```

**Accessibility requirements:**

- On form submission, the first erroneous input field automatically receives focus
- Error messages are associated with input fields via `aria-describedby`
- The sign-in button sets `aria-busy="true"` in the Loading state
- The form can be submitted with the `Enter` key

#### 5.1.2 Registration Page

**ASCII prototype:**

```
┌────────────────────────────────────────────────────────────┐
│                                                            │
│                   ┌─────────────┐                          │
│                   │    LOGO     │                          │
│                   └─────────────┘                          │
│                                                            │
│             ┌────────────────────────────────┐             │
│             │ Online Supermarket Registration│             │
│             │                                │             │
│             │  Username *                    │             │
│             │  [__________________________]  │             │
│             │                                │             │
│             │  Email *                       │             │
│             │  [__________________________]  │             │
│             │                                │             │
│             │  Password *                    │             │
│             │  [__________________________]  │             │
│             │                                │             │
│             │  Confirm Password *            │             │
│             │  [__________________________]  │             │
│             │                                │             │
│             │  Role:                         │             │
│             │  ○ Regular User  ○ Merchant    │             │
│             │                                │             │
│             │  [         Register         ]  │             │
│             │                                │             │
│             │  Already have an account?      │             │
│             │  Sign in                       │             │
│             └────────────────────────────────┘             │
│                                                            │
└────────────────────────────────────────────────────────────┘
```

**Interaction flow:**

```
User visits the registration page
    │
    ├─ Enter username
    │   └─ Validate on blur:
    │       ├─ Length 3-20 characters
    │       ├─ Only letters, digits, and underscores allowed
    │       └─ Asynchronously check whether the username already exists
    │
    ├─ Enter email
    │   └─ Validate email format on blur
    │
    ├─ Enter password
    │   └─ Show password strength indicator in real time (Weak/Medium/Strong)
    │
    ├─ Enter confirm password
    │   └─ Validate consistency with the password on blur
    │
    ├─ Select role (Regular User/Merchant)
    │   └─ When Merchant is selected, show the hint: "Merchant registration requires administrator approval"
    │
    └─ Click the "Register" button
        ├─ Validate all fields
        │   ├─ Errors found → scroll to the first erroneous field and focus it
        │   └─ No errors → call the registration API
        │       ├─ Success → show success message → automatically sign in and navigate
        │       └─ Failure → show error message
```

#### 5.1.3 Forgot Password Page

**ASCII prototype:**

```
┌────────────────────────────────────────────────────────────┐
│                                                            │
│           ┌──────────────────────────────────────┐         │
│           │           Reset Password             │         │
│           │                                      │         │
│           │  Step indicator:                     │         │
│           │  ① Email → ② New Password → ③ Done   │         │
│           │                                      │         │
│           │  ┌─ Current Step 1 ─┐                │         │
│           │  │ Please enter your │                │        │
│           │  │ registered email  │                │        │
│           │  │ [_______________] │                │        │
│           │  │                   │                │        │
│           │  │ [   Send Code   ] │                │        │
│           │  │                   │                │        │
│           │  │ Verification Code │                │        │
│           │  │ [__] [__] [__] [__]                │        │
│           │  │                   │                │        │
│           │  │ [     Next      ] │                │        │
│           │  └───────────────────┘                │        │
│           │                                      │         │
│           │  Back to Sign In                     │         │
│           └──────────────────────────────────────┘         │
│                                                            │
└────────────────────────────────────────────────────────────┘
```

**Interaction flow:**

```
Step 1: Verify email
    │
    ├─ Enter email → click "Send Code"
    │   ├─ Button enters a 60s countdown (disabled)
    │   └─ Show hint: "Verification code sent to your email"
    │
    ├─ Enter the 4-digit verification code
    │   └─ Automatically focus the next input field
    │
    └─ Click "Next" → verification passes and proceed to Step 2

Step 2: Enter new password
    │
    ├─ Enter a new password (show strength indicator)
    ├─ Confirm the new password
    └─ Click "Submit" → password reset succeeds

Step 3: Done
    │
    └─ Show success message → automatically navigate to the sign-in page after 3 seconds
```

---

### 5.2 Product Module

#### 5.2.1 Home Page (Product List)

**ASCII prototype:**

```
┌────────────────────────────────────────────────────────────┐
│  LOGO  [🔍 Search products...]     🛒 Cart   👤 Avatar        │
├────────────────────────────────────────────────────────────┤
│  All │ Electronics │ Clothing │ Food │ Home │ ...          │
├────────────────────────────────────────────────────────────┤
│                                                            │
│  Filter: [Price ▼] [Sales ▼] [Date ▼] [Brand ▼] [Reset]    │
│                                                            │
├────────────────────────────────────────────────────────────┤
│                                                            │
│  ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────┐│
│  │           │  │           │  │           │  │           ││
│  │   Image   │  │   Image   │  │   Image   │  │   Image   ││
│  │           │  │           │  │           │  │           ││
│  ├───────────┤  ├───────────┤  ├───────────┤  ├───────────┤│
│  │ Product A │  │ Product B │  │ Product C │  │ Product D ││
│  │ ¥99.00    │  │ ¥199.00   │  │ ¥59.00    │  │ ¥299.00   ││
│  │ 100 sold  │  │ 50 sold   │  │ 200 sold  │  │ 30 sold   ││
│  │[Add Cart] │  │[Add Cart] │  │[Add Cart] │  │[Add Cart] ││
│  └───────────┘  └───────────┘  └───────────┘  └───────────┘│
│                                                            │
│  ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────┐│
│  │           │  │           │  │           │  │           ││
│  │   Image   │  │   Image   │  │   Image   │  │   Image   ││
│  │           │  │           │  │           │  │           ││
│  └───────────┘  └───────────┘  └───────────┘  └───────────┘│
│                                                            │
├────────────────────────────────────────────────────────────┤
│  Total 100  [10/page ▼]  « 1 2 3 4 5 ... 10 »  Go [__]     │
├────────────────────────────────────────────────────────────┤
│         © 2025 Online Supermarket. All rights reserved.    │
└────────────────────────────────────────────────────────────┘
```

**Interaction flow:**

```
User visits the home page
    │
    ├─ Page load
    │   ├─ Show Loading skeleton screen
    │   ├─ Request the product list API (sorted by sales by default)
    │   └─ Render product cards after data returns (staggered fade-in animation)
    │
    ├─ Category navigation click
    │   ├─ Highlight the current category
    │   ├─ Re-request products in that category
    │   └─ Scroll to the top of the list
    │
    ├─ Filter changes
    │   ├─ Price sort: toggle ascending/descending
    │   ├─ Sales sort: toggle high→low/low→high
    │   ├─ Brand filter: multi-select dropdown
    │   └─ Click "Reset" to clear all filters
    │
    ├─ Product card interaction
    │   ├─ Hover: card lifts 4px + shadow appears
    │   ├─ Click image/name: navigate to the product detail page
    │   └─ Click "Add to Cart":
    │       ├─ Not signed in → navigate to the sign-in page
    │       ├─ Signed in → call the API
    │       │   ├─ Success → cart icon shows quantity + success message
    │       │   └─ Failure → show error message
    │       └─ Button shows a brief Loading state
    │
    └─ Pagination interaction
        ├─ Click a page number: refresh products on the current page
        ├─ Change page size: re-request and return to the first page
        └─ Enter a page number to jump: validate the page range
```

**Accessibility requirements:**

- The product list uses `role="list"`, and each product card uses `role="listitem"`
- Product images must have an `alt` attribute (the product name)
- Prices are wrapped in `<span aria-label="Price">`
- The "Add to Cart" button uses `aria-label="Add [product name] to cart"`
- When filters change, use `aria-live="polite"` to announce changes in the result count

#### 5.2.2 Product Detail Page

**ASCII prototype:**

```
┌────────────────────────────────────────────────────────────┐
│  LOGO  [🔍 Search products...]     🛒 Cart   👤 Avatar        │
├────────────────────────────────────────────────────────────┤
│  Home > Electronics > Phones > Smartphones > Details       │
├────────────────────────────────────────────────────────────┤
│                                                            │
│  ┌──────────────────┐  ┌──────────────────────────────────┐│
│  │                  │  │ Product Name                     ││
│  │                  │  │ ¥ 199.00                         ││
│  │    Main Image    │  │ 100 sold  |  Stock: 50           ││
│  │                  │  │                                  ││
│  │  [◀]      [▶]    │  │ Brand: XXX                       ││
│  └──────────────────┘  │ Category: Electronics > Phones   ││
│  ┌──┬──┬──┬──┐         │ Merchant: XXX Flagship Store     ││
│  │Th│Th│Th│Th│         │                                  ││
│  └──┴──┴──┴──┘         │ Quantity:  [-] [ 1 ] [+]         ││
│                        │ [🛒 Add to Cart]  [⚡ Buy Now]    │ │
│                        └──────────────────────────────────┘│
├────────────────────────────────────────────────────────────┤
│  [Product Details]  [Specifications]  [Reviews (20)]       │
├────────────────────────────────────────────────────────────┤
│                                                            │
│  Rich-text product description...                          │
│  (images, text, tables, etc.)                              │
│                                                            │
├────────────────────────────────────────────────────────────┤
│         © 2025 Online Supermarket. All rights reserved.    │
└────────────────────────────────────────────────────────────┘
```

**Interaction flow:**

```
User enters the product detail page
    │
    ├─ Page load
    │   ├─ Show skeleton screen (image area + text area)
    │   ├─ Request the product detail API
    │   └─ Render after data returns
    │
    ├─ Image carousel interaction
    │   ├─ Click a thumbnail: switch the main image
    │   ├─ Click the left/right arrows: previous/next image
    │   └─ Click the main image: open the large-image preview dialog
    │
    ├─ Quantity selector
    │   ├─ Click "+": quantity +1 (not exceeding stock)
    │   ├─ Click "-": quantity -1 (minimum 1)
    │   ├─ Direct input: validate the range on blur
    │   └─ When stock is exceeded, disable the "+" button and show a hint
    │
    ├─ Add to cart
    │   ├─ Not signed in → navigate to the sign-in page
    │   ├─ Signed in → call the API
    │   │   ├─ Success → success message + cart quantity update
    │   │   └─ Failure → error message
    │   └─ Button Loading state
    │
    ├─ Buy now
    │   ├─ Not signed in → navigate to the sign-in page
    │   └─ Signed in → navigate to the checkout page (carrying the current product and quantity)
    │
    └─ Tab switching
        ├─ Product Details: show the rich-text description
        ├─ Specifications: show the specification table
        └─ Reviews: show the review list (paginated)
```

#### 5.2.3 Search Results Page

**ASCII prototype:**

```
┌────────────────────────────────────────────────────────────┐
│  LOGO  [🔍 phone _______________]     🛒 Cart  👤 Avatar      │
├────────────────────────────────────────────────────────────┤
│  Search results: "phone" — 100 related products found      │
├────────────────────────────────────────────────────────────┤
│  Sort: [Relevance] [Sales] [Price ↑] [Price ↓] [Newest]    │
├────────────────────────────────────────────────────────────┤
│                                                            │
│  ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────┐│
│  │           │  │           │  │           │  │           ││
│  │   Image   │  │   Image   │  │   Image   │  │   Image   ││
│  │           │  │           │  │           │  │           ││
│  ├───────────┤  ├───────────┤  ├───────────┤  ├───────────┤│
│  │ Product A │  │ Product B │  │ Product C │  │ Product D ││
│  │ ¥99.00    │  │ ¥199.00   │  │ ¥59.00    │  │ ¥299.00   ││
│  │[Add Cart] │  │[Add Cart] │  │[Add Cart] │  │[Add Cart] ││
│  └───────────┘  └───────────┘  └───────────┘  └───────────┘│
│                                                            │
├────────────────────────────────────────────────────────────┤
│  Total 100  « 1 2 3 4 5 ... 10 »                           │
└────────────────────────────────────────────────────────────┘
```

**Interaction flow:**

```
User enters a search keyword
    │
    ├─ Search box autocomplete
    │   ├─ Show dropdown suggestions while typing (300ms debounce)
    │   ├─ Navigate the suggestion list with ↑/↓
    │   └─ Press Enter or click a suggestion: perform the search
    │
    ├─ Perform the search
    │   ├─ Show Loading state
    │   ├─ Request the search API
    │   │   ├─ Results found → render the product list
    │   │   └─ No results → show empty state + recommended products
    │   └─ Update the page title: "Search results: XXX"
    │
    └─ Sort switching
        ├─ Click a sort option: re-request and re-render
        └─ Highlight the current sort method
```

---

### 5.3 Cart Module

#### 5.3.1 Cart Page

**ASCII prototype:**

```
┌────────────────────────────────────────────────────────────┐
│  LOGO  [🔍 Search products...]     🛒 Cart   👤 Avatar        │
├────────────────────────────────────────────────────────────┤
│  Home > Cart                                               │
├────────────────────────────────────────────────────────────┤
│                                                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ ☐ Select All                       Delete Selected    │ │
│  ├──────────────────────────────────────────────────────┤  │
│  │ ☑ ┌─────┐  Product A                       ¥99.00    │  │
│  │   │Image│  Merchant: XXX Flagship Store              │  │
│  │   └─────┘  [-] [2] [+]         ¥198.00  [Delete]     │  │
│  ├──────────────────────────────────────────────────────┤  │
│  │ ☑ ┌─────┐  Product B                       ¥59.00    │  │
│  │   │Image│  Merchant: YYY Flagship Store              │  │
│  │   └─────┘  [-] [1] [+]          ¥59.00  [Delete]     │  │
│  ├──────────────────────────────────────────────────────┤  │
│  │ ☐ ┌─────┐  Product C (Low Stock)           ¥199.00   │  │
│  │   │Image│  Merchant: ZZZ Flagship Store              │  │
│  │   └─────┘  [-] [5] [+]         ¥995.00  [Delete]     │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ Selected: 2  Total: ¥257.00      [ Checkout (2) ]    │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                            │
├────────────────────────────────────────────────────────────┤
│         © 2025 Online Supermarket. All rights reserved.    │
└────────────────────────────────────────────────────────────┘
```

**Interaction flow:**

```
User visits the cart page
    │
    ├─ Page load
    │   ├─ Request the cart list API
    │   ├─ Render the product list
    │   └─ Products with insufficient stock are displayed with a warning style
    │
    ├─ Select all / deselect all
    │   ├─ Click the "Select All" checkbox: toggle the selected state of all products
    │   └─ Deselect an individual product: the Select All checkbox becomes unchecked
    │
    ├─ Quantity modification
    │   ├─ Click the +/- buttons: update the quantity (validate stock in real time)
    │   ├─ Direct input: validate on blur
    │   ├─ Exceeds stock: show a warning and limit the maximum value
    │   └─ Update the subtotal and the bottom total
    │
    ├─ Delete products
    │   ├─ Click "Delete": show a confirmation dialog
    │   │   ├─ Confirm: call the delete API → re-render
    │   │   └─ Cancel: close the dialog
    │   └─ Support batch deletion (delete selected)
    │
    ├─ Proceed to checkout
    │   ├─ No products selected: show "Please select products"
    │   ├─ Products with insufficient stock: show "Some products are out of stock"
    │   └─ Normal: navigate to the checkout page (carrying the list of selected product IDs)
    │
    └─ Empty cart
        └─ Show empty state + "Go Shopping" button → navigate to the home page
```

**Accessibility requirements:**

- The Select All checkbox uses `aria-label="Select All"`
- Each product checkbox uses `aria-label="Select [product name]"`
- When the quantity changes, use `aria-live="polite"` to announce subtotal changes
- The delete confirmation dialog uses `role="alertdialog"`
- The checkout button shows the selected count: `aria-label="Checkout, 2 items selected, total 257 CNY"`

---

### 5.4 Order Module

#### 5.4.1 Checkout Page

**ASCII prototype:**

```
┌────────────────────────────────────────────────────────────┐
│  LOGO  [🔍 Search products...]     🛒 Cart   👤 Avatar        │
├────────────────────────────────────────────────────────────┤
│  Home > Checkout                                           │
├────────────────────────────────────────────────────────────┤
│                                                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ Shipping Address                                     │  │
│  │ Zhang San  138****1234                               │  │
│  │ No. XXX, XXX Street, Chaoyang District, Beijing      │  │
│  │ [Set as Default] [Change Address]                    │  │
│  │ [ + Add Address ]                                    │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ Product List                                         │  │
│  ├──────────────────────────────────────────────────────┤  │
│  │ ┌─────┐  Product A  x2                   ¥198.00    │   │
│  │ │Image│  Merchant: XXX Flagship Store               │   │
│  │ └─────┘                                             │   │
│  ├──────────────────────────────────────────────────────┤  │
│  │ ┌─────┐  Product B  x1                    ¥59.00    │   │
│  │ │Image│  Merchant: YYY Flagship Store               │   │
│  │ └─────┘                                             │   │
│  └──────────────────────────────────────────────────────┘  │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ Payment Method                                       │  │
│  │ ○ Card Payment    ○ QR Code Payment                  │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ Order Note: [_________________________________]       │ │
│  └──────────────────────────────────────────────────────┘  │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ Product Total: ¥257.00                               │  │
│  │ Shipping Fee: ¥0.00                                  │  │
│  │ ─────────────────                                    │  │
│  │ Total Payable: ¥257.00                               │  │
│  │                              [  Place Order  ]       │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                            │
├────────────────────────────────────────────────────────────┤
│         © 2025 Online Supermarket. All rights reserved.    │
└────────────────────────────────────────────────────────────┘
```

**Interaction flow:**

```
User enters the checkout page
    │
    ├─ Page load
    │   ├─ Request the address list and product details
    │   ├─ Select the default address by default
    │   └─ Calculate the order total
    │
    ├─ Address selection
    │   ├─ Click "Change Address": show the address selection dialog
    │   ├─ Select an address: update the shipping address
    │   └─ Click "Add Address": navigate to the address management page
    │
    ├─ Payment method selection
    │   ├─ Select "Card Payment": navigate to the card payment page after submission
    │   └─ Select "QR Code Payment": show a QR code dialog after submission
    │
    ├─ Place order
    │   ├─ Validate: a shipping address is required
    │   ├─ Re-validate stock (to prevent stock changes during checkout)
    │   │   ├─ Insufficient stock: notify the user and block submission
    │   │   └─ Sufficient stock: call the create-order API
    │   │       ├─ Success: navigate to the payment page
    │   │       └─ Failure: show an error message
    │   └─ Button Loading state
    │
    └─ Return to cart
        └─ Click the Logo or "Back": show "Going back will clear the current checkout"
```

#### 5.4.2 Order List Page

**ASCII prototype:**

```
┌────────────────────────────────────────────────────────────┐
│  LOGO  [🔍 Search products...]     🛒 Cart   👤 Avatar        │
├────────────────────────────────────────────────────────────┤
│  Home > My Orders                                          │
├────────────────────────────────────────────────────────────┤
│  [All] [To Pay] [Paid] [Shipped] [Completed] [Cancelled]   │
├────────────────────────────────────────────────────────────┤
│                                                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ Order ORD202501010001  2025-01-01 10:30     [To Pay] │  │
│  ├──────────────────────────────────────────────────────┤  │
│  │ ┌─────┐  Product A  x1                   ¥99.00      │  │
│  │ │Image│  Merchant: XXX Flagship Store               │   │
│  │ └─────┘                                             │   │
│  ├──────────────────────────────────────────────────────┤  │
│  │ 1 item  Total: ¥99.00                                │  │
│  │                        [Cancel Order] [Pay Now]       │ │
│  └──────────────────────────────────────────────────────┘  │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ Order ORD202412310002  2024-12-31 15:20      [Done]  │  │
│  ├──────────────────────────────────────────────────────┤  │
│  │ ┌─────┐  Product B  x2                  ¥118.00      │  │
│  │ │Image│  Merchant: YYY Flagship Store               │   │
│  │ └─────┘                                             │   │
│  ├──────────────────────────────────────────────────────┤  │
│  │ 2 items  Total: ¥118.00                              │  │
│  │         [Logistics] [Confirm] [After-sales]          │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                            │
├────────────────────────────────────────────────────────────┤
│  Total 20  « 1 2 3 4 »                                     │
├────────────────────────────────────────────────────────────┤
│         © 2025 Online Supermarket. All rights reserved.    │
└────────────────────────────────────────────────────────────┘
```

**Interaction flow:**

```
User visits the order list
    │
    ├─ Page load
    │   ├─ Request the order list API (all statuses by default)
    │   └─ Render the order card list
    │
    ├─ Status filtering
    │   ├─ Click a status tab: re-request orders with the corresponding status
    │   └─ Highlight the currently selected tab
    │
    ├─ Order actions
    │   ├─ Pending Payment orders:
    │   │   ├─ "Cancel Order": show confirmation → call the API → update the status
    │   │   └─ "Pay Now": navigate to the payment page
    │   ├─ Paid orders:
    │   │   └─ "View Logistics": show the logistics information dialog
    │   ├─ Shipped orders:
    │   │   └─ "Confirm Receipt": show confirmation → call the API → update the status
    │   └─ Completed orders:
    │       └─ "Request After-sales": navigate to the after-sales request page
    │
    └─ Order details
        └─ Click the order number/product: navigate to the order detail page
```

#### 5.4.3 Payment Page

**ASCII prototype (card payment):**

```
┌────────────────────────────────────────────────────────────┐
│  LOGO  [🔍 Search products...]     🛒 Cart   👤 Avatar        │
├────────────────────────────────────────────────────────────┤
│  Home > My Orders > Payment                                │
├────────────────────────────────────────────────────────────┤
│                                                            │
│             ┌────────────────────────────────┐             │
│             │  Order Payment                 │             │
│             │                                │             │
│             │  Order No.: ORD202501010001    │             │
│             │  Amount Due: ¥ 257.00          │             │
│             │                                │             │
│             │  Payment Method: Card Payment  │             │
│             │                                │             │
│             │  Card Number                   │             │
│             │  [__________________________]  │             │
│             │                                │             │
│             │  Expiry (MM/YY)    CVV         │             │
│             │  [______/____]     [____]      │             │
│             │                                │             │
│             │  [      Confirm Payment      ] │             │
│             │                                │             │
│             │  🔒 Your payment information   │              │
│             │     is encrypted               │             │
│             └────────────────────────────────┘             │
│                                                            │
├────────────────────────────────────────────────────────────┤
│         © 2025 Online Supermarket. All rights reserved.    │
└────────────────────────────────────────────────────────────┘
```

**ASCII prototype (QR code payment):**

```
┌────────────────────────────────────────────────────────────┐
│  LOGO  [🔍 Search products...]     🛒 Cart   👤 Avatar        │
├────────────────────────────────────────────────────────────┤
│  Home > My Orders > Payment                                │
├────────────────────────────────────────────────────────────┤
│                                                            │
│             ┌────────────────────────────────┐             │
│             │  Order Payment                 │             │
│             │                                │             │
│             │  Order No.: ORD202501010001    │             │
│             │  Amount Due: ¥ 257.00          │             │
│             │                                │             │
│             │  Payment Method: QR Code       │             │
│             │                                │             │
│             │  ┌─────────────────────┐       │             │
│             │  │                     │       │             │
│             │  │    QR Code Image    │       │             │
│             │  │                     │       │             │
│             │  └─────────────────────┘       │             │
│             │                                │             │
│             │  Please scan the QR code with  │             │
│             │  your phone to pay             │             │
│             │  Click confirm after payment   │             │
│             │                                │             │
│             │  [      I Have Paid      ]     │             │
│             │                                │             │
│             │  🔒 Simulated payment.         │              │
│             │     No real charge is incurred.│             │
│             └────────────────────────────────┘             │
│                                                            │
├────────────────────────────────────────────────────────────┤
│         © 2025 Online Supermarket. All rights reserved.    │
└────────────────────────────────────────────────────────────┘
```

**Interaction flow (card payment):**

```
User enters the payment page
    │
    ├─ Page load
    │   ├─ Request order details
    │   └─ Display order information and the payment form
    │
    ├─ Card number input
    │   ├─ Real-time Luhn algorithm validation
    │   │   ├─ Valid format: show a green checkmark
    │   │   └─ Invalid format: show a red error message
    │   └─ Automatically format the display (add a space every 4 digits)
    │
    ├─ Expiry date input
    │   ├─ Format validation: MM/YY
    │   └─ Expiry validation: cannot be earlier than the current month
    │
    ├─ CVV input
    │   ├─ Format validation: 3 or 4 digits
    │   └─ Input type: password (masked)
    │
    └─ Confirm payment
        ├─ Validate all fields
        │   ├─ Errors found: focus the first erroneous field
        │   └─ No errors: call the payment API
        │       ├─ Success: show success message → navigate to the order details
        │       └─ Failure: show an error message
        └─ Button Loading state
```

**Accessibility requirements:**

- The payment amount uses `aria-label="Amount due: 257 CNY"`
- The card number input uses `aria-describedby` to associate the format hint
- The CVV input uses `type="password"` and includes a security hint
- The payment button sets `aria-busy="true"` in the Loading state
- The security notice text is visible to screen readers

---

#### 5.4.4 Order Detail Page

**ASCII prototype:**

```
┌────────────────────────────────────────────────────────────┐
│  LOGO  [🔍 Search products...]     🛒 Cart   👤 Avatar        │
├────────────────────────────────────────────────────────────┤
│  Home > My Orders > Order Details                          │
├────────────────────────────────────────────────────────────┤
│  Order No.: ORD202501010001       Status: [To Pay]         │
│  Order Time: 2025-01-01 10:30                              │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ Progress: Submit > Pay > Ship > Receive > Done       │  │
│  │ (Current node highlighted; completed nodes checked)  │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ Shipping Address                                     │  │
│  │ Zhang San  138****1234                               │  │
│  │ No. XXX, XXX Street, Chaoyang District, Beijing      │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ Product List                                         │  │
│  │ ┌─────┐  Product A  x2                   ¥198.00     │  │
│  │ │Image│  Merchant: XXX Flagship Store               │   │
│  │ └─────┘                                             │   │
│  └──────────────────────────────────────────────────────┘  │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ Product Total: ¥257.00  Shipping: ¥0.00  Due: ¥257.00│  │
│  │                         [Cancel Order] [Pay Now]      │ │
│  └──────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────┘
```

**Interaction flow:**

```
User enters the order detail page
    │
    ├─ Page load: request the order detail API
    │   └─ Render the product list, shipping address, amount breakdown, and status progress bar
    │
    ├─ Display action buttons according to order status
    │   ├─ Pending Payment → [Cancel Order] [Pay Now]
    │   ├─ Shipped → [Confirm Receipt]
    │   ├─ Received → [Request Refund/Return]
    │   └─ Completed → [Review Product]
    │
    ├─ Cancel Order: secondary confirmation → call the cancel API → restore stock
    ├─ Pay Now: navigate to the payment page
    ├─ Confirm Receipt: secondary confirmation → call the receipt API
    └─ Request Refund/Return: navigate to the request page (select products, enter reason/type)
```

---

### 5.5 User Center Module

#### 5.5.1 Personal Information Page

**ASCII prototype:**

```
┌────────────────────────────────────────────────────────────┐
│  LOGO  [🔍 Search products...]     🛒 Cart   👤 Avatar        │
├────────────────────────────────────────────────────────────┤
│  Home > User Center > Personal Information                 │
├────────────────────────────────────────────────────────────┤
│                                                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ Basic Information                                    │  │
│  ├──────────────────────────────────────────────────────┤  │
│  │ Avatar                                               │  │
│  │ ┌─────┐                                              │  │
│  │ │     │  [Change Avatar]                             │  │
│  │ └─────┘                                              │  │
│  │                                                      │  │
│  │ Username: zhangsan                 [Not editable]    │  │
│  │                                                      │  │
│  │ Nickname: Zhang San                [Edit]            │  │
│  │                                                      │  │
│  │ Email: zhangsan@example.com        [Edit]            │  │
│  │                                                      │  │
│  │ Phone: 138****1234                 [Edit]            │  │
│  │                                                      │  │
│  │ Role: Regular User                                   │  │
│  │                                                      │  │
│  │ Registration Time: 2025-01-01                        │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ Change Password                                      │  │
│  ├──────────────────────────────────────────────────────┤  │
│  │ Current Password: [________________]                 │  │
│  │ New Password:     [________________]                 │  │
│  │ Confirm Password: [________________]                 │  │
│  │                                                      │  │
│  │ [Save]  [Cancel]                                     │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                            │
├────────────────────────────────────────────────────────────┤
│         © 2025 Online Supermarket. All rights reserved.    │
└────────────────────────────────────────────────────────────┘
```

**Interaction flow:**

```
User visits the personal information page
    │
    ├─ Page load
    │   └─ Request the user information API and render
    │
    ├─ Edit nickname/email/phone
    │   ├─ Click "Edit": the input becomes editable
    │   ├─ Enter a new value: validate the format on blur
    │   ├─ Click "Save": call the update API
    │   │   ├─ Success: show success message + update the display
    │   │   └─ Failure: show an error message
    │   └─ Click "Cancel": restore the original value
    │
    ├─ Change avatar
    │   ├─ Click "Change Avatar": show the image picker
    │   ├─ Select an image: preview + crop
    │   └─ Confirm upload: call the upload API → update the avatar
    │
    └─ Change password
        ├─ Enter the current password, new password, and confirm password
        ├─ Real-time validation:
        │   ├─ Current password is not empty
        │   ├─ New password strength (at least 8 characters, containing letters and digits)
        │   └─ Confirm password matches the new password
        └─ Click "Save": call the change-password API
```

#### 5.5.2 Shipping Address Management Page

**ASCII prototype:**

```
┌────────────────────────────────────────────────────────────┐
│  LOGO  [🔍 Search products...]     🛒 Cart   👤 Avatar        │
├────────────────────────────────────────────────────────────┤
│  Home > User Center > Shipping Addresses                   │
├────────────────────────────────────────────────────────────┤
│                                                            │
│  [ + Add Address ]                                         │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ Zhang San  138****1234   [Default] [Edit] [Delete]   │  │
│  │ No. XXX, XXX Street, Chaoyang District, Beijing      │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ Li Si  139****5678  [Set as Default] [Edit] [Delete] │  │
│  │ No. YYY, YYY Road, Pudong New Area, Shanghai         │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                            │
├────────────────────────────────────────────────────────────┤
│         © 2025 Online Supermarket. All rights reserved.    │
└────────────────────────────────────────────────────────────┘
```

**Interaction flow:**

```
User visits the address management page
    │
    ├─ Page load
    │   └─ Request the address list and render
    │
    ├─ Add address
    │   ├─ Click "Add Address": show the address form dialog
    │   ├─ Fill in the form:
    │   │   ├─ Recipient (required)
    │   │   ├─ Phone number (required, format validation)
    │   │   ├─ Province/City/District (three-level cascading selector)
    │   │   ├─ Detailed address (required)
    │   │   └─ Set as default (checkbox)
    │   └─ Submit: call the create API → refresh the list
    │
    ├─ Edit address
    │   ├─ Click "Edit": show the address form dialog (pre-filled)
    │   └─ Submit: call the update API → refresh the list
    │
    ├─ Delete address
    │   ├─ Click "Delete": show a confirmation dialog
    │   └─ Confirm: call the delete API → refresh the list
    │
    └─ Set as default
        ├─ Click "Set as Default": call the API
        └─ On success: the current address shows "Default" and the others show "Set as Default"
```

---

### 5.6 Instant Messaging Module

#### 5.6.1 Chat Page

**ASCII prototype:**

```
┌────────────────────────────────────────────────────────────────────────────────┐
│  LOGO  [🔍 Search products...]            🛒 Cart   👤 Avatar                     │
├──────────────────────────┬─────────────────────────────────────────────────────┤
│  Conversation List       │  Current Conversation: XXX Flagship Store           │
│                          │                                                     │
│  ┌────────────────────┐  │ ┌─────────────────────────────────────────────────┐ │
│  │● XXX Flagship Store│  │ │                                                 │ │
│  │  Hello, is this... │  │ │  Merchant  10:30                                │ │
│  │  10:30             │  │ │  Hello, how can I help you?                     │ │
│  ├────────────────────┤  │ ├─────────────────────────────────────────────────┤ │
│  │○ YYY Flagship Store│  │ │                                                 │ │
│  │  In stock          │  │ │  Me  10:32                                      │ │
│  │  10:25             │  │ │  Is this product in stock?                      │ │
│  ├────────────────────┤  │ ├─────────────────────────────────────────────────┤ │
│  │○ Customer Service  │  │ │  Merchant  10:33                                │ │
│  │  Welcome to inquire│  │ │  Yes, stock is sufficient.                      │ │
│  │  Yesterday         │  │ │  Please buy with confidence.                    │ │
│  └────────────────────┘  │ ├─────────────────────────────────────────────────┤ │
│                          │ │  [ Type a message... ]          [Send]          │ │
│                          │ └─────────────────────────────────────────────────┘ │
├──────────────────────────┴─────────────────────────────────────────────────────┤
│                    © 2025 Online Supermarket. All rights reserved.             │
└────────────────────────────────────────────────────────────────────────────────┘
```

**Interaction flow:**

```
User visits the chat page
    │
    ├─ Page load
    │   ├─ Establish a WebSocket connection
    │   ├─ Request the conversation list
    │   └─ Render the conversation list (the first one is selected by default)
    │
    ├─ Conversation list interaction
    │   ├─ Click a conversation: load the message history for that conversation
    │   ├─ Unread messages: show a blue dot marker
    │   └─ New message arrives: update the conversation preview and unread marker
    │
    ├─ Sending and receiving messages
    │   ├─ Type a message: supports multi-line text (Shift+Enter for a new line)
    │   ├─ Click "Send" or press Enter:
    │   │   ├─ Send the message via WebSocket
    │   │   ├─ The message immediately appears in the chat area (optimistic update)
    │   │   └─ Clear the input box
    │   └─ Receive messages:
    │       ├─ WebSocket receives a message
    │       ├─ Append to the chat area
    │       └─ Automatically scroll to the bottom
    │
    ├─ Connection status
    │   ├─ Connecting: show the "Connecting..." hint
    │   ├─ Connected: send and receive messages normally
    │   └─ Disconnected: show "Connection lost, reconnecting..."
    │       └─ Automatically reconnect (retry after 3 seconds)
    │
    └─ Product context
        └─ Start a chat from the product detail page: automatically include the product ID
```

**Accessibility requirements:**

- When a new message arrives, use `aria-live="polite"` to announce it
- The message list uses `role="log"` to mark it as a log area
- The input box uses `aria-label="Type a message"`
- The send button sets `aria-disabled="true"` in the disabled state
- The unread message count uses `aria-label` for announcement

---

### 5.7 Merchant Backend Module

#### 5.7.1 Merchant Backend Layout

**ASCII prototype:**

```
┌────────────────────┬───────────────────────────────────────────────────────────┐
│XXX Flagship Store  │  🔔 3    👤 Merchant Name  ▼                                │
│                    │                                                           │
│📦 Products          │                                                           │
│💬 Reviews           │                                                           │
│📋 Orders            │                                                           │
│💰 Refund Review     │                      <router-view />                      │
│📊 Sales Statistics  │                                                           │
│                    │                                                           │
│                    │                                                           │
│                    │                                                           │
└────────────────────┴───────────────────────────────────────────────────────────┘
```

> Sidebar menu: Products, Reviews, Orders, Refund Review, Sales Statistics. See 5.7.6 for Reviews.

#### 5.7.2 Product Management Page

**ASCII prototype:**

```
┌────────────────────┬───────────────────────────────────────────────────────────┐
│XXX Flagship Store  │  🔔 3    👤 Merchant Name  ▼                                │
├────────────────────┼───────────────────────────────────────────────────────────┤
│📦 Products          │  Product Management                                       │
│📋 Orders            │                                                           │
│💰 Refund Review     │  [ + New Product ]  [Search products...]                  │
│📊 Sales Statistics  │                                                           │
│                    │                                                           │
│                    │                                                           │
│                    │  ┌───┬─────┬─────────┬──────┬──────┬─────────┬─────────┐  │
│                    │  │ ☐ │ Img │Product  │ Price│ Stock│ Status  │ Actions │  │
│                    │  ├───┼─────┼─────────┼──────┼──────┼─────────┼─────────┤  │
│                    │  │ ☐ │ Img │Product A│ ¥99  │ 100  │ Listed  │ Edit    │  │
│                    │  │   │     │         │      │      │         │ Unlist  │  │
│                    │  │   │     │         │      │      │         │ Delete  │  │
│                    │  ├───┼─────┼─────────┼──────┼──────┼─────────┼─────────┤  │
│                    │  │ ☐ │ Img │Product B│ ¥199 │ 50   │ Unlisted│ Edit    │  │
│                    │  │   │     │         │      │      │         │ List    │  │
│                    │  │   │     │         │      │      │         │ Delete  │  │
│                    │  └───┴─────┴─────────┴──────┴──────┴─────────┴─────────┘  │
│                    │                                                           │
│                    │  Total 20  « 1 2 3 »                                      │
└────────────────────┴───────────────────────────────────────────────────────────┘
```

**Interaction flow:**

```
Merchant enters the product management page
    │
    ├─ Page load
    │   ├─ Request the merchant product list API
    │   └─ Render the table (image/name/price/stock/status/actions)
    │
    ├─ Add product
    │   ├─ Click "New Product": show the product form
    │   │   ├─ Enter the name, select a three-level category, brand, and description
    │   │   ├─ Set SKUs (specification template + combination of specification values; set price/stock for each)
    │   │   └─ Upload product images
    │   └─ Submit: call the create API → default status is Unlisted
    │
    ├─ Edit product
    │   ├─ Click "Edit": show the form (pre-filled)
    │   └─ Submit: call the update API
    │
    ├─ List/Unlist
    │   └─ Click the status toggle: call the status API → refresh the list
    │
    └─ Delete product
        ├─ Click "Delete": secondary confirmation dialog
        └─ Confirm: call the delete API (if referenced by a cart/order, show a message that deletion is not possible)
```

---

#### 5.7.3 Order Management Page

**ASCII prototype:**

```
┌────────────────────┬───────────────────────────────────────────────────────────┐
│XXX Flagship Store  │  🔔 3    👤 Merchant Name  ▼                                │
├────────────────────┼───────────────────────────────────────────────────────────┤
│📦 Products          │  Order Management                                         │
│📋 Orders            │                                                           │
│💰 Refund Review     │  Status: [All] [Pending] [Shipped] [Completed]            │
│📊 Sales Statistics  │                                                           │
│                    │                                                           │
│                    │  ┌─────────┬──────────┬─────┬───────┬────────┬─────────┐  │
│                    │  │ Order   │ Product  │ Qty │ Amount│ Status │ Actions │  │
│                    │  ├─────────┼──────────┼─────┼───────┼────────┼─────────┤  │
│                    │  │ ORD001  │ Product A│ 2   │ ¥198  │ Pending│ [Ship]  │  │
│                    │  │ ORD002  │ Product B│ 1   │ ¥59   │ Shipped│ Details │  │
│                    │  └─────────┴──────────┴─────┴───────┴────────┴─────────┘  │
│                    │                                                           │
│                    │  Total 15  « 1 2 3 »                                      │
└────────────────────┴───────────────────────────────────────────────────────────┘
```

**Interaction flow:**

```
Merchant enters the order management page
    │
    ├─ Page load: request the merchant order list and render the table
    │
    ├─ Status filtering: click a status tab → re-request orders with the corresponding status
    │
    ├─ Ship
    │   ├─ Click "Ship": secondary confirmation
    │   └─ Confirm: call the ship API → order status becomes "Shipped"
    │
    └─ View details: click "Details" → show the order details (products/address/amount)
```

---

#### 5.7.4 Refund Review Page

**ASCII prototype:**

```
┌────────────────────┬───────────────────────────────────────────────────────────┐
│XXX Flagship Store  │  🔔 3    👤 Merchant Name  ▼                                │
├────────────────────┼───────────────────────────────────────────────────────────┤
│📦 Products          │  Refund Review                                            │
│📋 Orders            │                                                           │
│💰 Refund Review     │  Status: [Pending Review] [Approved] [Rejected]           │
│📊 Sales Statistics  │                                                           │
│                    │                                                           │
│                    │  ┌──────────┬─────────┬───────┬───────┬───────┬────────┐  │
│                    │  │ Request  │ Product │ Type  │ Amount│ Status│ Actions│  │
│                    │  ├──────────┼─────────┼───────┼───────┼───────┼────────┤  │
│                    │  │ RF001    │Product A│ Refund│ ¥99   │Pending│ Approve│  │
│                    │  │          │         │       │       │       │ Reject │  │
│                    │  │ RF002    │Product B│ Return│ ¥59   │Pending│ Approve│  │
│                    │  └──────────┴─────────┴───────┴───────┴───────┴────────┘  │
└────────────────────┴───────────────────────────────────────────────────────────┘
```

**Interaction flow:**

```
Merchant enters the refund review page
    │
    ├─ Page load: request the refund request list
    │
    ├─ Approve
    │   ├─ Click "Approve": show a confirmation
    │   └─ Confirm: call the review API → status "Under Review" (refund) or "Awaiting Return" (return)
    │
    ├─ Reject
    │   ├─ Click "Reject": show a reason input box
    │   └─ Submit: call the review API → status "Rejected"
    │
    └─ Return receipt confirmation: after receiving the returned item, click "Confirm Receipt" → process the refund
```

---

#### 5.7.5 Sales Statistics Page

**ASCII prototype:**

```
┌────────────────────┬───────────────────────────────────────────────────────────┐
│XXX Flagship Store  │  🔔 3    👤 Merchant Name  ▼                                │
├────────────────────┼───────────────────────────────────────────────────────────┤
│📦 Products          │  Sales Statistics   Time: [7 Days] [30 Days]              │
│📋 Orders            │                                                           │
│💰 Refund Review     │                                                           │
│📊 Sales Statistics  │                                                           │
│                    │                                                           │
│                    │  ┌───────────────────────────────────────────────────────┐│
│                    │  │  Sales Trend (Line Chart)                             ││
│                    │  │         ╱╲     ╱╲                                     ││
│                    │  │    ╱╲  ╱  ╲  ╱  ╲                                     ││
│                    │  │   ╱  ╲╱    ╲╱    ╲                                    ││
│                    │  └───────────────────────────────────────────────────────┘│
│                    │                                                           │
│                    │  Total Sales: ¥12,580   Orders: 86   Units Sold: 342      │
│                    │                                                           │
│                    │  Best-selling Ranking:                                    │
│                    │  ┌───────────────────────────────────────────────────────┐│
│                    │  │ 1. Product A 120 units  2. Product B 80 units ...     ││
│                    │  └───────────────────────────────────────────────────────┘│
└────────────────────┴───────────────────────────────────────────────────────────┘
```

**Interaction flow:**

```
Merchant enters the sales statistics page
    │
    ├─ Page load: request the statistics API (last 7 days by default)
    │   └─ Render the sales trend chart + total sales/order count/units sold cards
    │
    ├─ Time range switch: Last 7 Days / Last 30 Days → re-request and refresh the chart
    │
    └─ Best-selling ranking: display the top N products by sales
```

---

#### 5.7.6 Review Management Page

**ASCII prototype:**

```
┌────────────────────┬───────────────────────────────────────────────────────────┐
│XXX Flagship Store  │  🔔 3    👤 Merchant Name  ▼                                │
├────────────────────┼───────────────────────────────────────────────────────────┤
│📦 Products          │  Reviews          Product: [All Products]                 │
│💬 Reviews           │                                                           │
│📋 Orders            │                                                           │
│💰 Refund Review     │                                                           │
│📊 Sales Statistics  │                                                           │
│                    │  ┌───────────────────────────────────────────────────────┐│
│                    │  │ User A ★★★★★  2026-11-01                              ││
│                    │  │ Great product, fast shipping                          ││
│                    │  │ Follow-up Review: Used it for a week,                 ││
│                    │  │ quality is still great                                ││
│                    │  │ Merchant Reply: Thanks for your support!              ││
│                    │  │                                          [Reply]      ││
│                    │  └───────────────────────────────────────────────────────┘│
└────────────────────┴───────────────────────────────────────────────────────────┘
```

**Interaction flow:**

```
Merchant enters the review management page
    ├─ Select product: use the dropdown to filter reviews for a specific product
    ├─ List display: initial review / follow-up review / historical merchant replies / star rating and time
    └─ Reply: click "Reply" → enter content in the dialog → POST /api/comment/reply
        ├─ Success → show "Reply successful" → refresh the review list
        └─ Failure → display the backend error message
```

---

#### 5.7.7 Merchant Onboarding Application Page

**ASCII prototype:**

```
┌────────────────────────────────────────────────────────────┐
│  LOGO  [🔍 Search products...]     🛒 Cart   👤 Avatar        │
├────────────────────────────────────────────────────────────┤
│  Merchant Onboarding                                       │
├────────────────────────────────────────────────────────────┤
│                                                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ Store Name *        [__________________________]     │  │
│  │                                                      │  │
│  │ Contact Phone *     [__________________________]     │  │
│  │                                                      │  │
│  │ Business License    [ Upload Image ]                 │  │
│  │                                                      │  │
│  │ ⚠ Administrator approval is required after           │  │
│  │   submission. Once approved, you can sign in to      │  │
│  │   the merchant backend.                              │  │
│  │                                                      │  │
│  │                    [  Submit Application  ]          │  │
│  └──────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────┘
```

**Interaction flow:**

```
User enters the merchant onboarding page
    │
    ├─ Enter the store name (required, 2-50 characters)
    ├─ Enter the contact phone (required, 11-digit mobile number validation)
    ├─ Upload the business license (optional)
    │
    └─ Click "Submit Application"
        ├─ Validate the form
        │   ├─ Errors found → focus the first erroneous field
        │   └─ No errors → call the onboarding API
        │       ├─ Success → show "Submitted, awaiting review"
        │       └─ Failure (already submitted) → show "Application already submitted"
        └─ On successful submission, navigate to the merchant backend
```

---

### 5.8 Administrator Backend Module

#### 5.8.1 Administrator Backend Layout

**ASCII prototype:**

```
┌────────────────────┬───────────────────────────────────────────────────────────┐
│Admin Panel         │  🔔 5    👤 Administrator  ▼                                │
│                    │                                                           │
│👤 Users             │                                                           │
│🏬 Merchant Review   │                                                           │
│🗂 Categories        │                      <router-view />                      │
│📦 Products          │                                                           │
│📋 Orders            │                                                           │
│📊 Sales Reports     │                                                           │
│🎟 Flash-sale Coupons│                                                           │
│                    │                                                           │
└────────────────────┴───────────────────────────────────────────────────────────┘
```

> The layout structure is the same as the merchant backend: a fixed sidebar on the left + a content area, with 7 menu items.

#### 5.8.2 User Management Page

**ASCII prototype:**

```
┌────────────────────┬───────────────────────────────────────────────────────────┐
│Admin Panel         │  User Management   [Search users...] [Role ▾] [Status ▾]  │
├────────────────────┼───────────────────────────────────────────────────────────┤
│👤 Users             │                                                           │
│                    │                                                           │
│                    │  ┌──────────┬─────────┬─────────┬─────────┬────────────┐  │
│                    │  │ Username │ Role    │ Phone   │ Status  │ Actions    │  │
│                    │  ├──────────┼─────────┼─────────┼─────────┼────────────┤  │
│                    │  │ zhangsan │ User    │ 138...  │ Active  │ [Disable]  │  │
│                    │  │ lisi     │ Merchant│ 139...  │ Active  │ [Disable]  │  │
│                    │  │ wangwu   │ User    │ 137...  │ Disabled│ [Enable]   │  │
│                    │  └──────────┴─────────┴─────────┴─────────┴────────────┘  │
└────────────────────┴───────────────────────────────────────────────────────────┘
```

**Interaction flow:**

```
Administrator enters the user management page
    ├─ Load the user list (pagination + keyword/role/status filters)
    └─ Enable/Disable: click to toggle → secondary confirmation → call the status API
```

#### 5.8.3 Merchant Review Page

**ASCII prototype:**

```
┌────────────────────┬───────────────────────────────────────────────────────────┐
│Admin Panel         │  Merchant Review  [All] [Pending] [Approved] [Rejected]   │
├────────────────────┼───────────────────────────────────────────────────────────┤
│🏬 Merchant Review   │                                                           │
│                    │                                                           │
│                    │  ┌──────────┬─────────┬──────┬─────────┬───────────────┐  │
│                    │  │ Store    │ Contact │ Time │ Status  │ Comment       │  │
│                    │  ├──────────┼─────────┼──────┼─────────┼───────────────┤  │
│                    │  │ XX Flags │ Zhang   │ 10-28│ Pending │ —             │  │
│                    │  │ YY Market│ Li Si   │ 10-26│ Approved│ Docs OK       │  │
│                    │  │ ZZ Shop  │ Wang Wu │ 10-25│ Rejected│ Blurry license│  │
│                    │  └──────────┴─────────┴──────┴─────────┴───────────────┘  │
│                    │  Total 12  « 1 2 »                                        │
└────────────────────┴───────────────────────────────────────────────────────────┘
```

**Interaction flow:**

```
Administrator enters the merchant review page
    ├─ Load all merchants by default; supports All/Pending Review/Approved/Rejected filtering and pagination
    ├─ Approve: click "Approve" → secondary confirmation → call the review API (approved=1)
    ├─ Reject: click "Reject" → enter the rejection reason → call the review API (approved=0)
    └─ Reviewed records show the review comment; switch statuses to look up historical applications
```

#### 5.8.4 Category Management Page

**ASCII prototype:**

```
┌────────────────────┬───────────────────────────────────────────────────────────┐
│Admin Panel         │  Category Management                   [ + New Category ] │
├────────────────────┼───────────────────────────────────────────────────────────┤
│🗂 Categories        │                                                           │
│                    │  ┌───────────────────────────────────────────────────────┐│
│                    │  │ ▼ Electronics                                         ││
│                    │  │     ├─ ▼ Mobile Phones                                ││
│                    │  │     │     ├─ Smartphones   [Edit] [Delete]            ││
│                    │  │     │     └─ Feature Phones [Edit] [Delete]           ││
│                    │  │     └─ ▼ Computers & Office                           ││
│                    │  │  ▼ Clothing & Shoes                                   ││
│                    │  └───────────────────────────────────────────────────────┘│
└────────────────────┴───────────────────────────────────────────────────────────┘
```

**Interaction flow:**

```
Administrator enters the category management page
    ├─ Load the category tree (three levels)
    ├─ Add: click "New Category" → enter the name + select the parent category → submit
    │   └─ Validation: at most three levels; once the third level is reached, no more subcategories can be added
    ├─ Edit: click "Edit" → modify the name → save
    └─ Delete: click "Delete" → secondary confirmation → deletion is prohibited when subcategories exist
```

#### 5.8.5 Product Management Page (Platform-wide)

**ASCII prototype:**

```
┌────────────────────┬───────────────────────────────────────────────────────────┐
│Admin Panel         │  Product Management  [Search] [Category ▾] [Status ▾]     │
├────────────────────┼───────────────────────────────────────────────────────────┤
│📦 Products          │                                                           │
│                    │                                                           │
│                    │  ┌────────┬─────────┬───────┬───────┬─────────┬────────┐  │
│                    │  │ Product│ Merchant│ Price │ Stock │ Status  │ Actions│  │
│                    │  ├────────┼─────────┼───────┼───────┼─────────┼────────┤  │
│                    │  │ Prod. A│ XX Store│ ¥99   │ 100   │ Listed  │ Unlist │  │
│                    │  │ Prod. B│ YY Store│ ¥199  │ 50    │ Unlisted│ List   │  │
│                    │  └────────┴─────────┴───────┴───────┴─────────┴────────┘  │
└────────────────────┴───────────────────────────────────────────────────────────┘
```

**Interaction flow:**

```
Administrator enters the product management page
    ├─ Load the platform-wide product list (pagination + keyword/category/status filters)
    └─ List/Unlist: click to toggle → call the status API
```

#### 5.8.6 Order Management Page (Platform-wide)

**ASCII prototype:**

```
┌────────────────────┬───────────────────────────────────────────────────────────┐
│Admin Panel         │  Order Management   [Search order no.] [Status ▾]         │
├────────────────────┼───────────────────────────────────────────────────────────┤
│📋 Orders            │                                                           │
│                    │                                                           │
│                    │  ┌──────────┬──────────┬───────┬─────────┬─────────────┐  │
│                    │  │ Order No.│ User     │ Amount│ Status  │ Time        │  │
│                    │  ├──────────┼──────────┼───────┼─────────┼─────────────┤  │
│                    │  │ ORD001   │ Zhang San│ ¥198  │ Paid    │ 10-28 10:30 │  │
│                    │  │ ORD002   │ Li Si    │ ¥59   │ Done    │ 10-27 15:20 │  │
│                    │  └──────────┴──────────┴───────┴─────────┴─────────────┘  │
└────────────────────┴───────────────────────────────────────────────────────────┘
```

**Interaction flow:**

```
Administrator enters the order management page
    ├─ Load the platform-wide order list (pagination + order number/status filters)
    └─ View details: click an order → show the order details
```

#### 5.8.7 Sales Reports Page

**ASCII prototype:**

```
┌────────────────────┬───────────────────────────────────────────────────────────┐
│Admin Panel         │  Sales Reports  Time: [7 Days] [30 Days] [Custom]         │
├────────────────────┼───────────────────────────────────────────────────────────┤
│📊 Sales Reports     │                                                           │
│                    │                                                           │
│                    │  ┌───────────────────────────────────────────────────────┐│
│                    │  │  Platform-wide Sales Trend (Line Chart)               ││
│                    │  │       ╱╲   ╱╲    ╱╲                                   ││
│                    │  │   ╱╲╱  ╲╱  ╲  ╱  ╲                                    ││
│                    │  └───────────────────────────────────────────────────────┘│
│                    │                                                           │
│                    │  Total Sales: ¥89,200  Orders: 1200  Active Merchants: 15 │
│                    │                                                           │
│                    │  ┌───────────────────────────────────────────────────────┐│
│                    │  │ Category Share / Merchant Ranking / Best-selling      ││
│                    │  └───────────────────────────────────────────────────────┘│
└────────────────────┴───────────────────────────────────────────────────────────┘
```

**Interaction flow:**

```
Administrator enters the sales reports page
    ├─ Load global sales statistics (last 7 days by default)
    │   └─ Render the trend chart + total sales/order count/merchant count cards
    ├─ Time range switch: Last 7 Days / Last 30 Days / Custom range
    └─ Multi-dimensional reports: category share, merchant ranking, best-selling products
```

#### 5.8.8 Flash-sale Coupon Management Page

**ASCII prototype:**

```
┌────────────────────┬───────────────────────────────────────────────────────────┐
│Admin Panel         │  Flash-sale Coupon Management       [ + New Coupon ]      │
├────────────────────┼───────────────────────────────────────────────────────────┤
│🎟 Flash-sale Coupons│                                                           │
│                    │                                                           │
│                    │  ┌────────┬─────────┬───────┬─────────┬────────┬───────┐  │
│                    │  │ Name   │ Type    │ Disc. │Threshold│ Remain │ Action│  │
│                    │  ├────────┼─────────┼───────┼─────────┼────────┼───────┤  │
│                    │  │New User│ Fixed   │ ¥10   │ ¥50     │ 987    │ [Edit]│  │
│                    │  │        │ Discount│       │         │        │ Delete│  │
│                    │  │Double11│ Percent.│ 15%off│ ¥100    │ 320    │ [Edit]│  │
│                    │  └────────┴─────────┴───────┴─────────┴────────┴───────┘  │
└────────────────────┴───────────────────────────────────────────────────────────┘
```

**Interaction flow:**

```
Administrator enters the flash-sale coupon management page
    ├─ Load the coupon list (pagination + status filtering)
    ├─ New coupon: click "New Coupon" → enter name/type/discount value/threshold/quantity/time → submit
    ├─ Edit coupon: click "Edit" → modify → save (when the total quantity increases, the remaining quantity increases accordingly)
    └─ Delete coupon: click "Delete" → secondary confirmation → only coupons that have not started can be deleted
```

---

### 5.9 Coupon Module

#### 5.9.1 Coupon Center Page

**ASCII prototype:**

```
┌────────────────────────────────────────────────────────────┐
│  LOGO  [🔍 Search products...]     🛒 Cart   👤 Avatar        │
├────────────────────────────────────────────────────────────┤
│  Coupon Center                             [ My Coupons ]  │
├────────────────────────────────────────────────────────────┤
│  ┌──────────────────┐  ┌──────────────────┐                │
│  │ New User Discount │  │ Double 11 Discount│              │
│  │ ¥50 → ¥10         │  │ 15% off storewide │              │
│  │ 987/1000 left     │  │ 320/500 left      │              │
│  │ Valid until 12-31 │  │ Valid until 12-31 │              │
│  │     [Claim Now]   │  │     [Claim Now]   │              │
│  └──────────────────┘  └──────────────────┘                │
│                                                            │
│  ┌──────────────────┐  ┌──────────────────┐                │
│  │ Weekend Deal      │  │ Member Deal       │              │
│  │ ¥100 → ¥20        │  │ 10% off storewide │              │
│  │ Not Started       │  │ 1890/2000 left    │              │
│  │ [Not Started]     │  │     [Claim Now]   │              │
│  └──────────────────┘  └──────────────────┘                │
└────────────────────────────────────────────────────────────┘
```

**Interaction flow:**

```
User enters the coupon center
    │
    ├─ Page load: request the flash-sale coupon list (public)
    │   └─ Render coupon cards (name/discount/remaining/validity/status)
    │
    ├─ Claim a coupon
    │   ├─ Click "Claim Now" (only coupons that are ongoing can be clicked)
    │   ├─ Not signed in → navigate to the sign-in page
    │   └─ Signed in → call the claim API
    │       ├─ Success → show "Claimed successfully" → remaining quantity -1
    │       └─ Failure → show a message (sold out / already claimed / not started)
    │
    └─ View my coupons: click "My Coupons" → display the claimed coupon list (status/validity)
```

---

#### 5.9.2 Coupon Detail Dialog

**ASCII prototype:**

```
┌──────────────── Coupon Details ────────────────┐
│ New User Discount Coupon                       │
│ Spend ¥50, save ¥10                            │
│ 987 / 1000 remaining · Limit 1 per person      │
│ Event Time: 2024-01-01 00:00 ~ 2026-12-31 23:59│
│                                                │
│                         [Close] [Claim Now]    │
└────────────────────────────────────────────────┘
```

**Interaction flow:**

```
User clicks a coupon card
    ├─ Request GET /api/seckill/detail/{id}
    ├─ The dialog displays: coupon name, discount details, usage threshold, remaining quantity, per-person limit, event time
    ├─ Click "Claim Now"
    │   ├─ Guest → navigate to the sign-in page (return to the coupon center after signing in)
    │   └─ Signed in → call the claim API
    │       ├─ Success → show "Claimed successfully" → refresh the remaining quantity
    │       └─ Failure → show a message (sold out / already claimed / not started / ended)
    └─ Click "Close" or the overlay → close the dialog
```

---

### 5.10 User Settings and Help Module

#### 5.10.1 Account Settings Page

**ASCII prototype:**

```
┌────────────────────────────────────────────────────────────┐
│  LOGO  [🔍 Search products...]     🛒 Cart   👤 Avatar        │
├────────────────────────────────────────────────────────────┤
│  Account Settings                                          │
├────────────────────────────────────────────────────────────┤
│  ┌──────────────────────────────────────────────────────┐  │
│  │ Avatar Settings                                      │  │
│  │ ┌─────┐   [ Change Avatar ] (upload image, ≤2MB)     │  │
│  │ │     │                                              │  │
│  │ └─────┘                                              │  │
│  ├──────────────────────────────────────────────────────┤  │
│  │ Notification Preferences                             │  │
│  │ ☑ Orders  ☑ Promotions  ☑ Chat                       │  │
│  ├──────────────────────────────────────────────────────┤  │
│  │ Display Preferences                                  │  │
│  │ ☐ Compact list (show more products per page)         │  │
│  │ ☑ Show decimal prices (¥99.00 / ¥99)                 │  │
│  └──────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────┘
```

**Interaction flow:**

```
User enters the account settings page
    ├─ Change avatar: select an image → validate (image format + ≤2MB) → upload API → update the avatar
    ├─ Notification preferences: toggle switches → save locally (localStorage)
    └─ Display preferences: take effect immediately after toggling (compact list / decimal prices)
```

#### 5.10.2 Help Center Page

**ASCII prototype:**

```
┌────────────────────────────────────────────────────────────┐
│  LOGO  [🔍 Search products...]     🛒 Cart   👤 Avatar        │
├────────────────────────────────────────────────────────────┤
│  Help Center                                               │
├────────────────────────────────────────────────────────────┤
│  Categories: [Shopping Guide] [Payment Methods]            │
│  [Shipping Information] [After-sales Service] [About Us]   │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ Shopping Guide                                       │  │
│  │ · How to register an account                         │  │
│  │ · How to search for and select products              │  │
│  │ · How to place an order and pay                      │  │
│  │ · How to request a refund/return                     │  │
│  └──────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────┘
```

**Interaction flow:**

```
User enters the help center
    └─ Click a category tab → switch to the corresponding help content (static text)
```
```
```
