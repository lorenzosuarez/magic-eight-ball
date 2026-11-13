import { GoogleAuth } from "google-auth-library";
import type { Answer, Asked } from "./ask";

/** What the model sends back, as far as an answer is read from it. The names are Google's own. */
export interface Generated {
  promptFeedback?: { blockReason?: string };
  candidates?: { finishReason?: string; content?: { parts?: { text?: string; thought?: boolean }[] } }[];
}

/**
 * The reply in the app's own terms, as the app reads one it asked for itself: turned down for
 * safety, or the text in the pieces it came in, without the thoughts the model may send along.
 * Nothing written at all is no answer, and is told as none.
 */
export function read(generated: Generated): Answer | undefined {
  const candidate = generated.candidates?.[0];
  if (generated.promptFeedback?.blockReason !== undefined || candidate?.finishReason === "SAFETY") {
    return { outcome: "blocked" };
  }
  const parts = (candidate?.content?.parts ?? [])
    .filter((part) => !part.thought && typeof part.text === "string")
    .map((part) => part.text as string);
  return parts.some((part) => part.trim().length > 0) ? { outcome: "written", parts } : undefined;
}

/** The model, as asking the ball needs it. A fake stands in for it where what is built on it is tested. */
export interface Model {
  /** Asks once. No answer, for whatever reason, is an error. */
  write(model: string, asked: Asked): Promise<Answer>;
}

// The model is asked as whoever the function runs as, in the project's own Google Cloud: no key
// for it is kept anywhere, and what is asked is handled under the project's terms with Google
// Cloud, which do not let Google use it for anything of its own.
const auth = new GoogleAuth({ scopes: "https://www.googleapis.com/auth/cloud-platform" });

// Where the model is asked. "global" lets Google answer from wherever it has room, which is what
// keeps an answer quick; a region's name keeps the request in that region instead.
const LOCATION = process.env.MODEL_LOCATION || "global";
const HOST = LOCATION === "global" ? "aiplatform.googleapis.com" : `${LOCATION}-aiplatform.googleapis.com`;

// The app waits four and a half seconds for an answer and then gives one of its own: an answer
// that comes later than this would be paid for and never read.
const PATIENCE_MS = 4_000;

export const model: Model = {
  async write(name, asked) {
    const project = await auth.getProjectId();
    const response = await fetch(
      `https://${HOST}/v1/projects/${project}/locations/${LOCATION}/publishers/google/models/${encodeURIComponent(name)}:generateContent`,
      {
        method: "POST",
        headers: { Authorization: `Bearer ${await auth.getAccessToken()}`, "Content-Type": "application/json" },
        body: JSON.stringify({
          systemInstruction: { parts: [{ text: asked.instruction }] },
          contents: [{ role: "user", parts: [{ text: asked.question }] }],
          // Sampling is left to the model, as the app leaves it.
          generationConfig: { maxOutputTokens: asked.maxOutputTokens },
        }),
        signal: AbortSignal.timeout(PATIENCE_MS),
      },
    );
    // A refusal is told by its status alone: what Google answers may repeat what was asked.
    if (!response.ok) throw Object.assign(new Error(`The model answered ${response.status}`), { code: `model/${response.status}` });
    const answer = read((await response.json()) as Generated);
    if (answer === undefined) throw Object.assign(new Error("The model wrote nothing"), { code: "model/empty" });
    return answer;
  },
};
