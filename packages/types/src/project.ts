import type { IsoDateTime } from "./common";

export interface Project {
  id: string;
  slug: string;
  name: string;
  createdAt: IsoDateTime;
}
