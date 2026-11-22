const CLASS = { 'Not Started': 't1', 'In Progress': 't2', Completed: 't3' };

export default function StatusTag({ status }) {
  return <span className={`tag ${CLASS[status] || 't1'}`}>{status}</span>;
}
