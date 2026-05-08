/**
 * design-handoff/icons/*.svg 를 Vite 자산으로 import 하여 URL 을 반환한다.
 * 사용 예: <img src={icon('search')} />
 */
const iconModules = import.meta.glob<string>(
  '../../design-handoff/icons/*.svg',
  { eager: true, query: '?url', import: 'default' },
);

const map: Record<string, string> = Object.fromEntries(
  Object.entries(iconModules).map(([path, url]) => {
    const name = path.split('/').pop()!.replace(/\.svg$/, '');
    return [name, url];
  }),
);

export function icon(name: string): string {
  const url = map[name];
  if (!url) {
    // eslint-disable-next-line no-console
    console.warn(`icon not found: ${name}`);
    return '';
  }
  return url;
}
