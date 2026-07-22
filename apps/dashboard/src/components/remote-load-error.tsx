import { Button, Card, CardContent, CardDescription, CardHeader, CardTitle } from "@repo/ui";
import { IconAlertTriangle, IconRefresh } from "@tabler/icons-react";

interface RemoteLoadErrorProps {
  remoteName: string;
  onRetry?: () => void;
}

/** Fallback UI when a federated remote fails to load. */
export function RemoteLoadError({ remoteName, onRetry }: RemoteLoadErrorProps) {
  return (
    <Card className="max-w-lg border-destructive/40">
      <CardHeader>
        <CardTitle className="flex items-center gap-2 text-base">
          <IconAlertTriangle className="size-4 text-destructive" aria-hidden />
          Failed to load {remoteName}
        </CardTitle>
        <CardDescription>
          The micro-frontend remote could not be loaded. Ensure that remote is running in
          development, then retry.
        </CardDescription>
      </CardHeader>
      {onRetry ? (
        <CardContent>
          <Button type="button" variant="outline" size="sm" onClick={onRetry}>
            <IconRefresh className="size-4" aria-hidden />
            Retry
          </Button>
        </CardContent>
      ) : null}
    </Card>
  );
}
