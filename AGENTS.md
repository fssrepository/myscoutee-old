# Stop Command Rule

If the user says `stop` (or a direct variant like `stop!`, `stoppp`, `stop thinking`), immediately halt all actions:
- no more tool calls
- no edits
- no planning/explanations
- reply with a brief acknowledgment only

Resume only after a new explicit user request.
