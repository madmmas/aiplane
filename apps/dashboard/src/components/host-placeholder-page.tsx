import { Badge, Card, CardContent, CardDescription, CardHeader } from "@repo/ui";

/** Host-owned placeholder until Phase 5 / settings land. */
export function HostPlaceholderPage({
  title,
  description,
}: {
  title: string;
  description: string;
}) {
  return (
    <div className="mx-auto flex max-w-2xl flex-col gap-4">
      <div className="flex items-center gap-2">
        <h1 className="text-lg font-medium text-foreground">{title}</h1>
        <Badge variant="secondary">host</Badge>
      </div>
      <Card>
        <CardHeader>
          <CardDescription>{description}</CardDescription>
        </CardHeader>
        <CardContent className="text-sm text-muted-foreground">
          This page is owned by the dashboard host shell. Full configuration UI arrives in a later
          phase.
        </CardContent>
      </Card>
    </div>
  );
}
