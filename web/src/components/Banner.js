export default function Banner({ title, sub, children }) {
  return (
    <div className="head">
      <div>
        <h1 className="h1">{title}</h1>
        {sub ? <p className="sub">{sub}</p> : null}
      </div>
      {children}
    </div>
  );
}
