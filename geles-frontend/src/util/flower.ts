export function mapFlowerToWithFavorite(
  flower: Omit<Flower, "favorite">,
  favoriteFlowerIds: number[]
): Flower {
  return { ...flower, favorite: favoriteFlowerIds.includes(flower.id) };
}

export function mapFlowersToWithFavorite(
  flowers: Omit<Flower, "favorite">[],
  favoriteFlowerIds: number[]
): Flower[] {
  return flowers.map(flower =>
    mapFlowerToWithFavorite(flower, favoriteFlowerIds)
  );
}
