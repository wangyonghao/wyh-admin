<template>
  <div class="mx-auto mb-7 w-full max-w-md">
    <h2
      class="mb-3 text-3xl/9 font-bold tracking-tight text-foreground lg:text-4xl"
    >
      <slot></slot>
    </h2>

    <p class="lg:text-md text-sm text-muted-foreground">
      <slot name="desc"></slot>
    </p>
  </div>
</template>
