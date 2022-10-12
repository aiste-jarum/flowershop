import { AxiosError } from "axios";

export function isAxiosError(e: any): e is AxiosError {
  return e.isAxiosError;
}

// Lithuanian word pluralization based on count
export function pluralize(
  unary: string, // dalykas
  plural: string, // dalykai
  decary: string, // dalykų
  count: number
) {
  const lastDigit = +count.toString().split("").slice(-1)[0];

  if (count < 0) {
    count = -count;
  }

  // Unary (dalykas)
  // Check for last digit 1
  if (count !== 11 && lastDigit === 1) {
    return unary;
  }

  // Decary (dalykų)
  // Check for last digit 0
  if (lastDigit === 0) {
    return decary;
  }
  // Check if between 11 and 19
  if (count >= 11 && count <= 19) {
    return decary;
  }

  // Plural (dalykai)
  return plural;
}
