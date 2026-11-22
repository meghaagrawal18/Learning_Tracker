// The server only stores three statuses, so the bar shows 0%, 50% or 100%.
const PERCENT = { 'Not Started': 0, 'In Progress': 50, Completed: 100 };

export default function ProgressBar({ status, label }) {
  const percent = PERCENT[status] ?? 0;
  return (
    <div
      className={`prog p${percent}`}
      role="progressbar"
      aria-label={label}
      aria-valuemin={0}
      aria-valuemax={100}
      aria-valuenow={percent}
    >
      <i />
    </div>
  );
}
