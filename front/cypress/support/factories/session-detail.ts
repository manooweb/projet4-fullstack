export interface TeacherResponse {
  id: number
  firstName: string
  lastName: string
  createdAt: string
  updatedAt: string
}

export interface SessionResponse {
  id: number
  name: string
  description: string
  date: string
  teacher_id: number
  users: number[]
  createdAt: string
  updatedAt: string
}

export const teacher: TeacherResponse = {
  id: 1,
  firstName: 'Teacher',
  lastName: 'Learn',
  createdAt: '2026-01-10T10:00:00.000Z',
  updatedAt: '2026-02-10T10:00:00.000Z',
}

export const summerTeacher: TeacherResponse = {
  id: 2,
  firstName: 'Summer',
  lastName: 'Learn',
  createdAt: '2026-01-10T10:00:00.000Z',
  updatedAt: '2026-02-10T10:00:00.000Z',
}

export function yogaSession(users: number[] = []): SessionResponse {
  return {
    id: 1,
    name: 'Yoga Session',
    description: 'A relaxing yoga session',
    date: '2026-08-05T15:49:18',
    teacher_id: teacher.id,
    users,
    createdAt: '2026-08-05T15:49:18',
    updatedAt: '2026-08-05T15:49:18',
  }
}

export function summerYogaSession(users: number[] = []): SessionResponse {
  return {
    id: 2,
    name: 'Summer Yoga Session',
    description: 'A relaxing summer yoga session',
    date: '2026-08-05T15:49:18',
    teacher_id: summerTeacher.id,
    users,
    createdAt: '2026-08-05T15:49:18',
    updatedAt: '2026-08-05T15:49:18',
  }
}
