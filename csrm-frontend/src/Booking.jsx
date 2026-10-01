import { useEffect, useState } from "react";
import api, { errMsg } from "./api";

const HOURS = [8, 9, 10, 11, 12, 13, 14, 15, 16, 17];
const pad = (n) => String(n).padStart(2, "0");
const today = () => new Date().toISOString().slice(0, 10);

export default function Booking({ user }) {
  const [resources, setResources] = useState([]);
  const [resourceId, setResourceId] = useState("");
  const [date, setDate] = useState(today());
  const [slots, setSlots] = useState([]);
  const [mine, setMine] = useState([]);
  const [notes, setNotes] = useState([]);   // notifications
  const [error, setError] = useState("");

  const notify = (text) => setNotes((old) => [text, ...old]);

  const loadResources = async () => {
    const res = await api.get("/resources");
    // students cannot book classrooms
    const list = res.data.filter((r) => r.availability && !(user.role === "STUDENT" && r.type === "CLASSROOM"));
    setResources(list);
    if (list.length > 0 && !resourceId) setResourceId(list[0].id);
  };

  const loadSlots = async () => {
    if (!resourceId) return;
    const res = await api.get("/bookings/slots", { params: { resourceId, date } });
    setSlots(res.data);
  };

  const loadMine = async () => {
    const res = await api.get("/bookings");
    setMine(res.data);
  };

  useEffect(() => { loadResources(); loadMine(); }, []);
  useEffect(() => { loadSlots(); }, [resourceId, date]);

  const isTaken = (h) => {
    const s = new Date(`${date}T${pad(h)}:00:00`);
    const e = new Date(`${date}T${pad(h + 1)}:00:00`);
    return slots.some((b) => new Date(b.startTime) < e && new Date(b.endTime) > s);
  };

  const book = async (h) => {
    setError("");
    try {
      await api.post("/bookings", {
        resourceId,
        startTime: `${date}T${pad(h)}:00`,
        endTime: `${date}T${pad(h + 1)}:00`,
      });
      notify(`Booking confirmed for ${date} ${pad(h)}:00 (email/SMS sent)`);
      loadSlots();
      loadMine();
    } catch (e) {
      setError(errMsg(e));
    }
  };

  const cancel = async (id) => {
    try {
      await api.delete("/bookings/" + id);
      notify(`Booking #${id} cancelled (email/SMS sent)`);
      loadSlots();
      loadMine();
    } catch (e) {
      setError(errMsg(e));
    }
  };

  const modify = async (id) => {
    const startTime = prompt("New start (format 2026-10-05T10:00)");
    const endTime = prompt("New end (format 2026-10-05T11:00)");
    if (!startTime || !endTime) return;
    try {
      await api.put("/bookings/" + id, { startTime, endTime });
      notify(`Booking #${id} modified (email/SMS sent)`);
      loadSlots();
      loadMine();
    } catch (e) {
      setError(errMsg(e));
    }
  };

  const statusOf = (b) => {
    if (b.status === "CANCELLED") return "CANCELLED";
    const now = new Date();
    if (new Date(b.endTime) < now) return "PAST";
    if (new Date(b.startTime) <= now) return "ONGOING";
    return "UPCOMING";
  };

  const nameOf = (id) => resources.find((r) => r.id === id)?.name || "Resource #" + id;

  return (
    <div>
      <h3>Book a resource</h3>
      <select value={resourceId} onChange={(e) => setResourceId(Number(e.target.value))}>
        {resources.map((r) => (
          <option key={r.id} value={r.id}>{r.name} ({r.type}, {r.location})</option>
        ))}
      </select>
      <input type="date" value={date} onChange={(e) => setDate(e.target.value)} />
      <p className="err">{error}</p>

      <table>
        <thead><tr><th>Time</th><th>Status</th><th></th></tr></thead>
        <tbody>
          {HOURS.map((h) => (
            <tr key={h} className={isTaken(h) ? "taken" : "free"}>
              <td>{pad(h)}:00 - {pad(h + 1)}:00</td>
              <td>{isTaken(h) ? "Booked" : "Free"}</td>
              <td>{!isTaken(h) && <button onClick={() => book(h)}>Book</button>}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <h3>My bookings</h3>
      <table>
        <thead><tr><th>ID</th><th>Resource</th><th>Start</th><th>End</th><th>Status</th><th></th></tr></thead>
        <tbody>
          {mine.map((b) => (
            <tr key={b.id}>
              <td>{b.id}</td>
              <td>{nameOf(b.resourceId)}</td>
              <td>{b.startTime.replace("T", " ")}</td>
              <td>{b.endTime.replace("T", " ")}</td>
              <td>{statusOf(b)}</td>
              <td>
                {b.status === "BOOKED" && (
                  <>
                    <button onClick={() => modify(b.id)}>Modify</button>
                    <button onClick={() => cancel(b.id)}>Cancel</button>
                  </>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <h3>Notifications</h3>
      {notes.length === 0 && <p>No notifications yet.</p>}
      {notes.map((n, i) => <div key={i} className="msg">{n}</div>)}
    </div>
  );
}
