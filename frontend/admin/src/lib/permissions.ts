export function computeCanCreate(roles: string[] = [], permissions: string[] = []): boolean {
  const hasRole = (r: string) => roles.includes(r);
  const hasPerm = (p: string) => permissions.includes(p);
  return hasRole("ROLE_ADMIN") || hasRole("ROLE_EDITOR") || hasPerm("DOC:CREATE");
}

export function hasRole(roles: string[] = [], role: string): boolean {
  return roles.includes(role);
}