import { Outfit, DM_Sans } from 'next/font/google';
import './globals.css';

const outfit = Outfit({ subsets: ['latin'], weight: ['500', '600'], variable: '--font-outfit' });
const dmSans = DM_Sans({ subsets: ['latin'], variable: '--font-dm' });

export const metadata = {
  title: 'Learning Tracker',
  description: 'Track training progress for your cohort.',
};

export default function RootLayout({ children }) {
  return (
    <html lang="en" className={`${outfit.variable} ${dmSans.variable}`}>
      <body>{children}</body>
    </html>
  );
}
