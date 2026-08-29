export function humanizeStatus(value: string | null | undefined): string {
  if (!value) return 'Not Set';
  return value.toLowerCase().split('_').map(part => part.charAt(0).toUpperCase() + part.slice(1)).join(' ');
}
