<template>
  <aside class="email-sidebar">
    <a :href="`mailto:${email}`" class="email-link">
      <span class="email-text">{{ email }}</span>
    </a>
    <div class="email-line"></div>
  </aside>
</template>

<script setup>
const email = 'sunrun.h@gmail.com'
</script>

<style scoped>
.email-sidebar {
  position: fixed;
  bottom: 0;
  right: 40px;
  width: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  z-index: 10;
}

.email-link {
  font-family: var(--font-mono);
  font-size: 12px;
  letter-spacing: 0.1em;
  color: var(--light-slate);
  writing-mode: vertical-rl;
  margin-bottom: 20px;
  transition: all 0.3s ease;
}

.email-link:hover {
  color: var(--green);
  opacity: 1;
  transform: translateY(-3px);
}

.email-text {
  display: block;
}

.email-line {
  width: 1px;
  height: 90px;
  background-color: var(--lightest-navy);
}
</style>
