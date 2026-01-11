import type { BaseLayoutProps } from 'fumadocs-ui/layouts/shared';
import Image from 'next/image';

export function baseOptions(): BaseLayoutProps {
  return {
    nav: {
      title: (
        <div className="flex items-center gap-2">
          <Image
            src="/logo.png"
            alt="Compose Camera Logo"
            width={32}
            height={32}
            style={{ width: '32px', height: '32px', objectFit: 'contain' }}
          />
          <span className="font-bold">Compose Camera</span>
        </div>
      ),
    },
    githubUrl: 'https://github.com/l2hyunwoo/compose-camera',
  };
}
